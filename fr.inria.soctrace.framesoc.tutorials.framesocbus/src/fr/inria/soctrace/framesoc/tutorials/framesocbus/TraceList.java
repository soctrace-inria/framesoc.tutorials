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
package fr.inria.soctrace.framesoc.tutorials.framesocbus;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;

/**
 * This class is an example tool illustrating the Framesoc bus tutorial. It
 * displays a list of the traces that are in the database and use the bus to
 * keep the list up to date.
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class TraceList extends FramesocTool {

	public TraceList() {
	}

	@Override
	public void launch(IFramesocToolInput input) {

		final IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		try {
			window.getActivePage().showView(TraceListView.ID);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
