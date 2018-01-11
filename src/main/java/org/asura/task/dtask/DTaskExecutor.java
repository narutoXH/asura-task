package org.asura.task.dtask;

import java.util.concurrent.TimeUnit;

import org.asura.task.exception.DependencyDoesNotExistException;

public interface DTaskExecutor {

	public void submit(DTaskGraph taskGraph) throws InterruptedException, DependencyDoesNotExistException;
	
	public void submit(DTaskGraph taskGraph, DTaskGraphListener listerner) throws InterruptedException, DependencyDoesNotExistException;

	public boolean awaitTermination(long timeout, TimeUnit units) throws InterruptedException;

	public void shutdown();

	public void shutdownNow();

	public boolean isShutdown();

	public boolean isTerminated();
			
	public void waitDTaskGraphCompleted(DTaskGraph taskGraph) throws InterruptedException;

}
