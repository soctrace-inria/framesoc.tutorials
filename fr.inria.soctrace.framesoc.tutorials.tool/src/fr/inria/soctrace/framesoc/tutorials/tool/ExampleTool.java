package fr.inria.soctrace.framesoc.tutorials.tool;

import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;

public class ExampleTool extends FramesocTool {
		
	@Override
	public void launch(IFramesocToolInput input) {
		System.out.println("Hello World!");
		ExampleToolInput exampleInput = (ExampleToolInput) input;
		System.out.println("My input is valid: " + exampleInput);
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
