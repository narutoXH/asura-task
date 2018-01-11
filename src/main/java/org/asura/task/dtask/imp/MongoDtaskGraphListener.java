package org.asura.task.dtask.imp;

import org.asura.data.log.MongoLogFactory;
import org.asura.task.dtask.DTask;
import org.asura.task.dtask.DTask.STATE;
import org.asura.task.dtask.DTaskGraph;
import org.asura.task.dtask.DTaskGraphListener;
import org.slf4j.LoggerFactory;

public class MongoDtaskGraphListener implements DTaskGraphListener {

	@Override
	public void end(DTaskGraph taskGraph) {
		MongoLogFactory.getLogger(taskGraph.getName()).info("task graph job finished. \n" + taskGraph);
		LoggerFactory.getLogger(taskGraph.getName()).info("task graph job finished. \n" + taskGraph);
	}

	@Override
	public void begin(DTaskGraph taskGraph) {
		MongoLogFactory.getLogger(taskGraph.getName()).info("task graph job started. \n" + taskGraph);
		LoggerFactory.getLogger(taskGraph.getName()).info("task graph job started. \n" + taskGraph);

	}

	@Override
	public void traceStatus(DTaskGraph taskGraph, DTask t, STATE state) {
		MongoLogFactory.getLogger(taskGraph.getName()).info("task " + t.getName() + " with state " + state.toString());
		LoggerFactory.getLogger(taskGraph.getName()).info("task " + t.getName() + " with state " + state.toString());
	}

}
