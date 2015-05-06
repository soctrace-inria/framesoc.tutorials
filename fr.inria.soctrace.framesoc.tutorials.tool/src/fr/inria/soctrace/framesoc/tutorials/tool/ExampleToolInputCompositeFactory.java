package fr.inria.soctrace.framesoc.tutorials.tool;

import org.eclipse.swt.widgets.Composite;

import fr.inria.soctrace.framesoc.ui.input.AbstractToolInputComposite;
import fr.inria.soctrace.framesoc.ui.input.AbstractToolInputCompositeFactory;

public class ExampleToolInputCompositeFactory extends
		AbstractToolInputCompositeFactory {

	public ExampleToolInputCompositeFactory() {
		// nothing to do
	}

	@Override
	public AbstractToolInputComposite getComposite(Composite parent, int style) {
		return new ExampleToolInputComposite(parent, style);
	}

}
