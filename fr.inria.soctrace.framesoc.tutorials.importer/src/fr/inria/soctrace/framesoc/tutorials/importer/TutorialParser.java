/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Youenn Corre - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.tutorials.importer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.IdManager;

/**
 * Class that handles the parsing of an example trace
 * 
 * @author "Youenn Corre" <youenn.corre@inria.fr>
 */
public class TutorialParser {

	// Specify how often we commit the events into the database
	private static final int PAGE_SIZE = 20000;

	// Constant specifying the values of the tags
	private static final String EVENT_PRODUCER_TAG = "P";
	private static final String EVENT_TAG = "E";

	// Constant defining the CSV separator
	private static final String CSV_SEPARATOR = ";";

	// Database objects
	private SystemDBObject systemDB;
	private TraceDBObject traceDB;
	// Path to the trace file
	private String traceFile;

	// ID managers
	private IdManager eIdManager = new IdManager();
	private IdManager etIdManager = new IdManager();
	private IdManager epIdManager = new IdManager();

	// List of the events that have not yet be saved
	private List<Event> eventList = new ArrayList<Event>();
	private int page;

	// Map of the event types with their name as key
	private Map<String, EventType> types = new HashMap<String, EventType>();

	// Map of event producers with their name as key
	private Map<String, EventProducer> producersMap = new HashMap<String, EventProducer>();

	// Number of processed events 
	private int numberOfEvents;

	/**
	 * Constructor
	 * 
	 * @param systemDB
	 *            system database object
	 * @param traceDB
	 *            trace database object
	 * @param traceFile
	 *            path to the trace file
	 */
	public TutorialParser(SystemDBObject systemDB, TraceDBObject traceDB,
			String traceFile) {
		this.systemDB = systemDB;
		this.traceDB = traceDB;
		this.traceFile = traceFile;
	}

	/**
	 * Perform the parsing operations
	 * 
	 * @param monitor
	 *            progress monitor
	 * @throws SoCTraceException
	 */
	public void parseTrace(IProgressMonitor monitor) throws SoCTraceException {
		try {
			// Set monitor main task and the quantity of work
			// Here since we don't know beforehand the quantity of work we use
			// the constant IProgressMonitor.UNKNOWN
			monitor.beginTask("Import trace", IProgressMonitor.UNKNOWN);
			monitor.subTask("Trace file: " + traceFile);

			// Create root producer
			EventProducer root = new EventProducer(epIdManager.getNextId());
			root.setName("root");
			root.setParentId(EventProducer.NO_PARENT_ID);
			root.setLocalId(root.getName());
			producersMap.put(root.getName(), root);

			// Create trace Events, EventTypes and Producers
			boolean part = parseRawTrace(monitor);

			// Save the other data
			saveProducers();
			saveTypes();
			saveTraceMetadata(part);
		} finally {
			monitor.done();
		}
	}

	private boolean parseRawTrace(IProgressMonitor monitor)
			throws SoCTraceException {
		try {
			// Init
			boolean partialImport = false;
			numberOfEvents = 0;
			page = 0;
			eventList.clear();

			// Open the file
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new DataInputStream(new FileInputStream(traceFile))));
			String[] line;
			String strLine;

			// Read a line
			while ((strLine = br.readLine()) != null) {

				// Remove extra white spaces
				strLine = strLine.trim();
				if (strLine.isEmpty())
					continue;

				// Split the line with the default separator 
				line = strLine.split(CSV_SEPARATOR);

				switch (line[0]) {
				// Event Producer
				case EVENT_PRODUCER_TAG:
					createEventProducer(line);
					break;

				// Event
				case EVENT_TAG:
					createEvent(line);
					break;

				default:
					// Ignore
					continue;
				}

				if (eventList.size() == PAGE_SIZE)
					page++;

				if (eventList.size() >= PAGE_SIZE) {
					// Save the current batch of events
					saveEvents(eventList);
					// Increase the number of processed events
					numberOfEvents += eventList.size();
					eventList.clear();
					// Check the monitor status
					if (monitor.isCanceled()) {
						partialImport = true;
						break;
					}
				}
			}

			// Close the file reader
			if (br != null)
				br.close();

			// Save the remaining events
			if (eventList.size() > 0) {
				saveEvents(eventList);
				numberOfEvents += eventList.size();
				eventList.clear();
			}

			return partialImport;
		} catch (Exception e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * Save the events of a page in the trace DB.
	 * 
	 * @param events
	 *            events list
	 * @throws SoCTraceException
	 */
	private void saveEvents(List<Event> events) throws SoCTraceException {
		for (Event e : events) {

			// Perform check on the event
			try {
				e.check();
			} catch (SoCTraceException ex) {
				throw new SoCTraceException(ex);
			}
			// Save the event
			traceDB.save(e);

			// Save the event parameters
			for (EventParam ep : e.getEventParams()) {
				traceDB.save(ep);
			}
		}

		// Store in database
		traceDB.commit(); // committing each page is faster
	}

	/**
	 * Method that creates an event producer and add it to the index of event
	 * producers
	 * 
	 * @param line
	 *            line of the trace file containing the element of the producer
	 * @throws SoCTraceException
	 */
	public void createEventProducer(String[] line) throws SoCTraceException {
		// Instantiate with unique ID
		EventProducer ep = new EventProducer(epIdManager.getNextId());
		ep.setName(line[2]);
		ep.setParentId(producersMap.get(line[1]).getId());
		ep.setLocalId(String.valueOf(ep.getId()));
		producersMap.put(ep.getName(), ep);
	}

	/**
	 * Create an event and add it to the list of events to save
	 * 
	 * @param line
	 *            the line describing the event
	 * @throws SoCTraceException
	 */
	public void createEvent(String[] line) throws SoCTraceException {
		// Instantiate with a unique ID
		Event e = new Event(eIdManager.getNextId());
		e.setEventProducer(producersMap.get(line[1]));
		e.setTimestamp(Long.valueOf(line[3]));
		e.setType(getType(line[2], EventCategory.PUNCTUAL_EVENT));
		e.setPage(page);
		eventList.add(e);
	}

	/**
	 * Save the event producers into the trace database
	 * 
	 * @throws SoCTraceException
	 */
	private void saveProducers() throws SoCTraceException {
		Collection<EventProducer> eps = producersMap.values();
		for (EventProducer ep : eps) {
			traceDB.save(ep);
		}
		traceDB.commit();
	}

	/**
	 * Save the event types into the trace database
	 * 
	 * @throws SoCTraceException
	 */
	private void saveTypes() throws SoCTraceException {
		for (EventType et : types.values()) {
			traceDB.save(et);
			for (EventParamType ept : et.getEventParamTypes()) {
				traceDB.save(ept);
			}
		}
	}

	/**
	 * Method that returns the event type of name name, and creates it if it
	 * does not exist
	 * 
	 * @param name
	 *            name of the event type
	 * @param category
	 *            category of the event type
	 * @return the corresponding event type
	 */
	private EventType getType(String name, int category) {
		// If the type does not exist
		if (!types.containsKey(name)) {
			// Create it
			EventType et = new EventType(etIdManager.getNextId(), category);
			et.setName(name);
			types.put(name, et);
		}
		return types.get(name);
	}

	/**
	 * Save the trace metadata
	 * 
	 * @param partialImport
	 *            specify if the trace was a partial import
	 * @throws SoCTraceException
	 */
	protected void saveTraceMetadata(boolean partialImport)
			throws SoCTraceException {
		String alias = FilenameUtils.getBaseName(traceFile);
		TutorialTraceMetadata metadata = new TutorialTraceMetadata(systemDB,
				traceDB.getDBName(), alias, numberOfEvents);
		metadata.createMetadata();
		metadata.saveMetadata();
	}
}
