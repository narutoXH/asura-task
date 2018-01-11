package org.asura.task.handler.rchandler;

import org.asura.core.data.DataRecord;

public abstract class ResChainHandler {

	protected ResChainHandler succesor;

	protected String name;

	protected String decription;

	public ResChainHandler getSuccesor() {
		return succesor;
	}

	public void setSuccesor(ResChainHandler succesor) {
		this.succesor = succesor;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDecription() {
		return decription;
	}

	public void setDecription(String decription) {
		this.decription = decription;
	}

	public abstract DataRecord process(DataRecord record, ResChainHandlerLog log);

}
