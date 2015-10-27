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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import fr.inria.soctrace.framesoc.core.tools.importers.AbstractTraceMetadataManager;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;

/**
 * Class to manage the trace metadata
 * 
 * @author "Youenn Corre" <youenn.corre@inria.fr>
 */
public class TutorialTraceMetadata extends AbstractTraceMetadataManager {

	protected String dbName;
	protected String alias;
	protected int events;

	@Override
	public String getTraceTypeName() {
		return "TutorialTrace";
	}

	public TutorialTraceMetadata(SystemDBObject sysDB, String dbName,
			String alias, int events) throws SoCTraceException {
		super(sysDB);
		this.dbName = dbName;
		this.alias = alias;
		this.events = events;
	}

	@Override
	public void setTraceFields(Trace trace) {
		trace.setAlias(alias);
		trace.setDbName(dbName);
		trace.setDescription("Example trace imported " + getCurrentDate());
		trace.setNumberOfCpus(1);
		trace.setNumberOfEvents(events);
		trace.setOutputDevice("Example trace importer");
		trace.setProcessed(false);
		trace.setTracedApplication("unknown");
		trace.setBoard("unknown");
		trace.setOperatingSystem("unknown");
	}

	/**
	 * Get the current date.
	 * 
	 * @return a string with the current date
	 */
	protected String getCurrentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.setTimeZone(TimeZone.getDefault());
		sdf.applyPattern("dd MMM yyyy HH:mm:ss z");
		return sdf.format(new Date()).toString();
	}

}
