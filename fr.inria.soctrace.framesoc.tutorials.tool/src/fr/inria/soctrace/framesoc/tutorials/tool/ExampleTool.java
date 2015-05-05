package fr.inria.soctrace.framesoc.tutorials.tool;

import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;

public class ExampleTool extends FramesocTool {
	
	public ExampleTool() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void launch(IFramesocToolInput input) {
		System.out.println("Hello World!");
	}
}
