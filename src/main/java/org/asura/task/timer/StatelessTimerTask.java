package org.asura.task.timer;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class StatelessTimerTask implements Job {
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Task task = (Task) context.getJobDetail().getJobDataMap().get("task");
		FlexTimer.logger.info("timer '" + task.getId() + "' start a new polling");
		task.getMethod().run();
		FlexTimer.logger.info("timer '" + task.getId() + "' end a new polling");
	}
}
