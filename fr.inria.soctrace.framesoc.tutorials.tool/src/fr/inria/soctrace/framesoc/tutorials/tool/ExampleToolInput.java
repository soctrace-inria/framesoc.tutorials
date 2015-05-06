package fr.inria.soctrace.framesoc.tutorials.tool;

import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;


public class ExampleToolInput implements IFramesocToolInput {

	public enum QueryEntity {
		PRODUCERS,
		TYPES;
	}
	
	private QueryEntity queryEntity;
	
	@Override
	public String getCommand() {
		return "";
	}

	public QueryEntity getQueryEntity() {
		return queryEntity;
	}

	public void setQueryEntity(QueryEntity queryEntity) {
		this.queryEntity = queryEntity;
	}

	@Override
	public String toString() {
		return "ExampleToolInput [queryEntity=" + queryEntity + "]";
	}

}
