package org.asura.task.batch;

public interface IBatchTask {
	
	public String getKey();

	public void process();
}
