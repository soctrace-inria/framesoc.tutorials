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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopicList;
import fr.inria.soctrace.framesoc.core.bus.IFramesocBusListener;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.TraceQuery;
import fr.inria.soctrace.lib.storage.SystemDBObject;

/**
 * This class is an example tool illustrating the Framesoc bus tutorial. It
 * displays a list of the traces that are in the database and use the bus to
 * keep the list up to date.
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class TraceListView extends ViewPart implements IFramesocBusListener {

	public static final String ID = "fr.inria.soctrace.framesoc.tutorials.framesocbus.TraceListView"; //$NON-NLS-1$
	public static final String PLUGIN_ID = Activator.PLUGIN_ID;

	// Combo displaying the list of traces
	private Combo comboTraces;
	// Map with the index of the traces in the combo
	final Map<Integer, Trace> traceMap = new HashMap<Integer, Trace>();

	// List of traces
	private List<Trace> traces;

	// Manage the bus topics
	private FramesocBusTopicList topics;

	public TraceListView() {
		// Register the topic necessary to synchronize traces
		topics = new FramesocBusTopicList(this);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_TRACES_SYNCHRONIZED);
		topics.registerAll();
	}

	@Override
	public void createPartControl(Composite parent) {
		// parent layout
		GridLayout gl_parent = new GridLayout(1, false);
		gl_parent.verticalSpacing = 2;
		gl_parent.marginWidth = 0;
		gl_parent.horizontalSpacing = 0;
		gl_parent.marginHeight = 0;
		parent.setLayout(gl_parent);

		final Group groupTraces = new Group(parent, SWT.NONE);
		groupTraces.setSize(422, 40);
		groupTraces.setForeground(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		groupTraces.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		groupTraces.setLayout(new GridLayout(2, false));
		groupTraces.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		comboTraces = new Combo(groupTraces, SWT.READ_ONLY);
		GridData gd_comboTraces = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1);
		gd_comboTraces.widthHint = 170;
		comboTraces.setLayoutData(gd_comboTraces);
		comboTraces.setToolTipText("Trace Selection");

		refreshTraceList();
	}

	@Override
	public void dispose() {
		// Unregister the subscribed topics
		if (topics != null)
			topics.unregisterAll();

		super.dispose();
	}

	@Override
	public void setFocus() {
	}

	@Override
	/**
	 * Will only receive message of the topic we subscribed to (i.e. TOPIC_UI_TRACES_SYNCHRONIZED).
	 * If we subscribed to more than one topics, it would have been necessary to check the received typ of message
	 */
	public void handle(FramesocBusTopic topic, Object data) {
		// Update the list of traces
		refreshTraceList();
	}

	/**
	 * Refresh the list of traces
	 */
	void refreshTraceList() {
		try {
			// Reload the trace list
			loadTraces();
		} catch (SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Update the displayed list of traces
		comboTraces.removeAll();
		int index = 0;
		for (final Trace t : traces) {
			comboTraces.add(t.getAlias(), index);
			traceMap.put(index, t);
			index++;
		}
	}

	/**
	 * Load the traces present in the database
	 * 
	 * @throws SoCTraceException
	 */
	public void loadTraces() throws SoCTraceException {
		final SystemDBObject sysDB = FramesocManager.getInstance()
				.getSystemDB();
		final TraceQuery tQuery = new TraceQuery(sysDB);
		traces = tQuery.getList();
		sysDB.close();

		// Sort alphabetically
		Collections.sort(traces, new Comparator<Trace>() {
			@Override
			public int compare(final Trace arg0, final Trace arg1) {
				return arg0.getAlias().compareTo(arg1.getAlias());
			}
		});
	}

}
