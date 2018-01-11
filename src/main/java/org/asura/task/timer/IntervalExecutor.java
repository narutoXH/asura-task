package org.asura.task.timer;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.asura.core.util.ExceptionUtil;
import org.slf4j.Logger;

public abstract class IntervalExecutor {
	private int interval;
	private boolean finish;
	private boolean daemMode;
	private String name;
	private long id;
	private boolean running;
	private Thread thread;
	private Logger logger;
	private int timeout;
	private static ExecutorService exec = Executors.newCachedThreadPool();

	public IntervalExecutor() {
		this.interval = 1000;
		this.finish = false;
		this.daemMode = false;
	}

	public int getTimeout() {
		return this.timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public int getInterval() {
		return this.interval;
	}

	public boolean isFinished() {
		return this.finish;
	}

	public void stop() {
		this.finish = true;
		this.running = false;
		this.thread.interrupt();
	}

	public abstract void execute();

	public void start() {
		this.finish = false;
		this.thread = new Thread(getRunnable());
		if (this.name == null) {
			this.name = String.valueOf(this.thread.getId());
		}
		this.thread.setName(this.name);
		this.thread.setDaemon(this.daemMode);
		this.thread.start();

		this.id = this.thread.getId();
	}

	public long getID() {
		return this.id;
	}

	public boolean isRunning() {
		return this.running;
	}

	private Runnable getRunnable() {
		return new Runnable() {
			public void run() {
				while (!(IntervalExecutor.this.finish)) {
					IntervalExecutor.this.running = true;
					try {
						if (IntervalExecutor.this.timeout > 0) {
							Future<Boolean> future = IntervalExecutor.exec.submit(IntervalExecutor.this.getCallable());
							try {
								if (IntervalExecutor.this.timeout > 0)
									future.get(IntervalExecutor.this.timeout, TimeUnit.SECONDS);
							} catch (InterruptedException e) {
								if (IntervalExecutor.this.logger != null)
									IntervalExecutor.this.logger.error("主线程在等待计算结果时被中断！");
							} catch (ExecutionException e) {
								if (IntervalExecutor.this.logger != null)
									IntervalExecutor.this.logger.error("主线程等待计算结果，但计算抛出异常！");
							} catch (TimeoutException e) {
								if (IntervalExecutor.this.logger != null) {
									IntervalExecutor.this.logger.error("主线程等待计算结果超时，因此中断任务线程！");
								}
								System.err.println("主线程等待计算结果超时，因此中断任务线程！");
								IntervalExecutor.exec.shutdownNow();
							}
						} else {
							IntervalExecutor.this.execute();
						}

						Thread.sleep(IntervalExecutor.this.interval);
					} catch (Exception e) {
						if (IntervalExecutor.this.logger != null) {
							IntervalExecutor.this.logger.error(
									IntervalExecutor.this.name + " failed.\n" + ExceptionUtil.getExceptionContent(e));
						}
					}
				}

				IntervalExecutor.this.running = false;
			}
		};
	}

	private Callable<Boolean> getCallable() {
		return new Callable<Boolean>() {
			public Boolean call() throws Exception {
				IntervalExecutor.this.execute();
				return Boolean.valueOf(true);
			}
		};
	}

	public boolean isDaemMode() {
		return this.daemMode;
	}

	public void setDaemMode(boolean daemMode) {
		this.daemMode = false;
	}

	public void setFinish(boolean finish) {
		this.running = (!(finish));
		this.finish = finish;
	}
}