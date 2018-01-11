package org.asura.task.batch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import org.asura.core.util.DateUtil;
import org.asura.core.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskBatch {

	private static final Logger logger = LoggerFactory.getLogger(TaskBatch.class);
	private boolean complete;
	private Stack<IBatchTask> tasks;
	private List<Thread> pool;
	private boolean addDone;
	private int total;
	private int doCount;
	private int pCount;

	public TaskBatch(int count) {
		this.tasks = new Stack<>();
		this.pCount = 1;
		this.pool = new ArrayList<>();
		for (int i = 0; i < count; ++i) {
			this.pool.add(new Thread(getRunnable()));
		}
	}

	public void setPrintCount(int count) {
		this.pCount = count;
	}

	public void start() {
		for (int i = 0; i < this.pool.size(); ++i) {
			this.pool.get(i).start();
		}
	}

	public void addDone() {
		this.addDone = true;
	}

	public void waitForDone() {
		for (int i = 0; i < this.pool.size(); ++i)
			try {
				this.pool.get(i).join();
			} catch (InterruptedException e) {
				logger.error(ExceptionUtil.getExceptionContent(e));
			}
	}

	public void stop() {
		this.complete = true;
	}

	public void addTask(IBatchTask task) {
		this.tasks.push(task);
		this.total += 1;
	}

	public int getTaskCount() {
		return this.tasks.size();
	}

	private Runnable getRunnable() {
		return new Runnable() {
			public void run() {
				while (true) {
					if ((TaskBatch.this.complete) || ((TaskBatch.this.addDone) && (TaskBatch.this.tasks.size() == 0))) {
						TaskBatch.logger.info(Thread.currentThread().getId() + " is completed");
						return;
					}
					IBatchTask task = null;
					synchronized (TaskBatch.this.tasks) {
						if (TaskBatch.this.tasks.size() > 0) {
							task = (IBatchTask) TaskBatch.this.tasks.pop();
							TaskBatch.this.doCount += 1;
							if (TaskBatch.this.doCount % TaskBatch.this.pCount == 0) {
								TaskBatch.logger.info(TaskBatch.this.doCount + " of " + TaskBatch.this.total + ", left "
										+ TaskBatch.this.tasks.size() + " at " + Thread.currentThread().getId() + " "
										+ DateUtil.getDateTimeString(new Date()));
							}
						}
					}
					if (task != null)
						try {
							task.process();
						} catch (Exception e) {
							TaskBatch.logger.error(ExceptionUtil.getExceptionContent(e));
						}
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						TaskBatch.logger.error(ExceptionUtil.getExceptionContent(e));
					}
				}
			}
		};
	}
}
