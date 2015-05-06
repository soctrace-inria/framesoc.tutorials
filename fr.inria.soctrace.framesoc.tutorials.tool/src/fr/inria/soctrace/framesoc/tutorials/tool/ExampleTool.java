package fr.inria.soctrace.framesoc.tutorials.tool;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;

public class ExampleTool extends FramesocTool {
		
	@Override
	public void launch(IFramesocToolInput input) {
		System.out.println("Hello World!");
		ExampleToolInput exampleInput = (ExampleToolInput) input;
		System.out.println("My input is valid: " + exampleInput);
		
		// open the view
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		try {
			ExampleView view = (ExampleView) window.getActivePage().showView(ExampleView.ID);
			view.setInput(exampleInput);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public ParameterCheckStatus canLaunch(IFramesocToolInput input) {
		if (input instanceof ExampleToolInput) {
			ExampleToolInput exampleInput = (ExampleToolInput) input;
			if (exampleInput.getQueryEntity() != null ) {
				System.out.println("Input is OK: " + exampleInput);
				return new ParameterCheckStatus(true, "");		
			}
		}
		return new ParameterCheckStatus(false, "Wrong input");
	}

}
