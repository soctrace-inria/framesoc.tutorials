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

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.core.tools.management.PluginImporterJob;
import fr.inria.soctrace.framesoc.core.tools.model.FileInput;
import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.framesoc.core.tools.model.IPluginToolJobBody;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;

/**
 * This class implements an example of simple importer to be studied in a
 * tutorial for the Framesoc framework.
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class ExampleImporter extends FramesocTool {
	
	public static final String TRACE_PREFIX = "Tutorial_";

	@Override
	public void launch(IFramesocToolInput input) {
		PluginImporterJob job = new PluginImporterJob("Tutorial Importer",
				new TutorialImporterPluginJobBody(input));
		job.setUser(true);
		job.schedule();
	}
	
	public class TutorialImporterPluginJobBody implements IPluginToolJobBody {

		// Input provided through the GUI
		private FileInput input;

		public TutorialImporterPluginJobBody(IFramesocToolInput input) {
			this.input = (FileInput) input;
		}

		@Override
		public void run(IProgressMonitor monitor) throws SoCTraceException {

			// Get all the files provided as arguments
			List<String> traces = input.getFiles();

			// For each trace file
			for (String traceFile : traces) {

				// Use the default trace name provider
				String traceDbName = FramesocManager.getInstance()
						.getTraceDBName(TRACE_PREFIX);

				SystemDBObject systemDB = null;
				TraceDBObject traceDB = null;

				try {
					// Open a connection to the System Database
					systemDB = SystemDBObject.openNewInstance();
					// Create a new Trace Database with the generated name
					traceDB = new TraceDBObject(traceDbName, DBMode.DB_CREATE);

					// Instantiate the parser 
					TutorialParser parser = new TutorialParser(systemDB, traceDB,
							traceFile);
					// and launch the parsing
					parser.parseTrace(monitor);

				} catch (SoCTraceException e) {
					e.printStackTrace();
					// Use the default import exception handler
					PluginImporterJob.catchImporterException(e, systemDB, traceDB);
				} finally {
					// Close the trace DB and the system DB (this will also
					// performs a commit)
					DBObject.finalClose(traceDB);
					DBObject.finalClose(systemDB);
				}
			}
		}
	}
	
	@Override
	public ParameterCheckStatus canLaunch(IFramesocToolInput input) {
		// FileInput is the default format for the input of the importer 
		FileInput args = (FileInput) input;

		// Check that there is at least one file
		if (args.getFiles().size() == 0) {
			return new ParameterCheckStatus(false,
					"Specify a trace file or a directory containing trace files.");
		}

		List<String> theFiles = args.getFiles();
		ParameterCheckStatus status = new ParameterCheckStatus(true, "");
		// Check that each file provided exists and can be read 
		for (String aFileName : theFiles) {
			File aFile = new File(aFileName);
			if (!aFile.exists() || !aFile.canRead()) {
				status.valid = false;
				status.message = "Invalid trace or illegal arguments passed";
				return status;
			}
		}
		return status;
	}

}
