package fr.inria.soctrace.framesoc.tutorials.tool;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import fr.inria.soctrace.framesoc.tutorials.tool.ExampleToolInput.QueryEntity;
import fr.inria.soctrace.framesoc.ui.utils.TraceSelection;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.query.EventTypeQuery;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;

public class ExampleView extends ViewPart {

	public final static String ID = "fr.inria.soctrace.framesoc.tutorials.tool.ExampleView"; //$NON-NLS-1$

	private Text txtTrace;
	private Text txtEntity;
	private QueryEntity queryEntity;
	private ListViewer listViewer;

	/**
	 * The listener we register with the selection service
	 */
	private ISelectionListener listener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			// we ignore our own selections
			if (sourcepart != ExampleView.this) {
				if (!TraceSelection.isSelectionValid(selection))
					return;
				List<Trace> traces = TraceSelection.getTracesFromSelection(selection);
				if (traces.isEmpty())
					return;
				updateView(traces.get(0));
			}
		}
	};

	@Override
	public void createPartControl(Composite parent) {

		/** Layout */
		parent.setLayout(new GridLayout(2, false));

		/** Selected trace */
		Label lblSelectedTrace = new Label(parent, SWT.NONE);
		lblSelectedTrace.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSelectedTrace.setText("Selected Trace");
		txtTrace = new Text(parent, SWT.BORDER);
		txtTrace.setEditable(false);
		txtTrace.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		/** Selected Entity */
		Label lblQueryEntity = new Label(parent, SWT.NONE);
		lblQueryEntity.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblQueryEntity.setText("Query Entity");
		txtEntity = new Text(parent, SWT.BORDER);
		txtEntity.setEditable(false);
		txtEntity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		// init default values
		queryEntity = QueryEntity.TYPES;
		txtEntity.setText(queryEntity.toString());

		/** Entities */
		new Label(parent, SWT.NONE);
		listViewer = new ListViewer(parent, SWT.BORDER | SWT.V_SCROLL);
		listViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		// we use an array content provider, since the input is a list of string
		listViewer.setContentProvider(new ArrayContentProvider());
		// use a base label provider, which simply calls the toString() methods on the
		// elements of the input
		listViewer.setLabelProvider(new LabelProvider());

		// register the selection listener
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(listener);

	}

	/**
	 * Updates the view with the current selected traces and the list of entities corresponding to
	 * the 'query entity'.
	 * 
	 * @param trace
	 *            selected trace
	 */
	private void updateView(Trace trace) {
		txtTrace.setText(trace.getAlias());
		List<String> input = new ArrayList<>();
		TraceDBObject traceDB = null;
		try {
			traceDB = TraceDBObject.openNewIstance(trace.getDbName());
			if (queryEntity.equals(QueryEntity.TYPES)) {
				EventTypeQuery etq = new EventTypeQuery(traceDB);
				List<EventType> etl = etq.getList();
				for (EventType et : etl) {
					input.add(et.getName());
				}
			} else if (queryEntity.equals(QueryEntity.PRODUCERS)) {
				EventProducerQuery epq = new EventProducerQuery(traceDB);
				List<EventProducer> epl = epq.getList();
				for (EventProducer ep : epl) {
					input.add(ep.getName());
				}
			}
		} catch (SoCTraceException e) {
			e.printStackTrace();
		} finally {
			DBObject.finalClose(traceDB);
		}
		listViewer.setInput(input);
	}

	public void setInput(ExampleToolInput exampleInput) {
		queryEntity = exampleInput.getQueryEntity();
		txtEntity.setText(queryEntity.toString());
	}

	@Override
	public void setFocus() {
		// nothing to do
	}

	@Override
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(listener);
		super.dispose();
	}

}
