package org.asura.task.timer;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

@SuppressWarnings("deprecation")
public class StatefulTimerTask implements StatefulJob {
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Task task = (Task) context.getJobDetail().getJobDataMap().get("task");
		FlexTimer.logger.info("timer '" + task.getId() + "' start a new polling");
		task.getMethod().run();
		FlexTimer.logger.info("timer '" + task.getId() + "' end a new polling");
	}
}