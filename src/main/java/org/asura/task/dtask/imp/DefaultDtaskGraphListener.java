package org.asura.task.dtask.imp;

import org.asura.task.dtask.DTask;
import org.asura.task.dtask.DTask.STATE;
import org.asura.task.dtask.DTaskGraph;
import org.asura.task.dtask.DTaskGraphListener;

public class DefaultDtaskGraphListener implements DTaskGraphListener{

	@Override
	public void end(DTaskGraph taskGraph) {
		System.out.println("job ended");
	}

	@Override
	public void begin(DTaskGraph taskGraph) {
		System.out.println("job started");
	}

	@Override
	public void traceStatus(DTaskGraph taskGraph, DTask t, STATE state) {
		System.out.println(t.getGroup()+"@@@"+t.getName()+"\t"+state.toString());
	}
	

}
