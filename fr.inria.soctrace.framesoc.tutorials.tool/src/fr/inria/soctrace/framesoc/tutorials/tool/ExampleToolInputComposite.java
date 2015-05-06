package fr.inria.soctrace.framesoc.tutorials.tool;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.framesoc.tutorials.tool.ExampleToolInput.QueryEntity;
import fr.inria.soctrace.framesoc.ui.input.AbstractToolInputComposite;

public class ExampleToolInputComposite extends AbstractToolInputComposite {

	// Tool input that will be passed to the tool by Framesoc when the tool is launched
	private ExampleToolInput input = new ExampleToolInput();

	public ExampleToolInputComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		final Button types = new Button(this, SWT.RADIO);
		types.setText("Event Types");
		types.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (types.getSelection()) {
					update(QueryEntity.TYPES);
				}
			}
		});

		// default value: types
		types.setSelection(true);
		input.setQueryEntity(QueryEntity.TYPES);

		final Button producers = new Button(this, SWT.RADIO);
		producers.setText("Event Producers");
		producers.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (producers.getSelection()) {
					update(QueryEntity.PRODUCERS);
				}
			}
		});
	}

	private void update(QueryEntity entity) {
		// react to input change
		input.setQueryEntity(entity);
		// ask the Framesoc dialog to update the OK button
		dialog.updateOk();
	}

	@Override
	public IFramesocToolInput getToolInput() {
		return input;
	}

}
