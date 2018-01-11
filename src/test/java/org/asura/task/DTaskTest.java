package org.asura.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.asura.task.dtask.AbstractDTask;
import org.asura.task.dtask.DTaskGraph;
import org.asura.task.dtask.MultiThreadedDTaskExecutor;
import org.asura.task.exception.DependencyDoesNotExistException;
import org.junit.Test;

import com.google.common.util.concurrent.Uninterruptibles;

public class DTaskTest {

	List<String> result = Collections.synchronizedList(new ArrayList<String>());

	@Test
	public void testDTaskGraph() {
		DTaskGraph dg = new DTaskGraph("test1");

		assertFalse(dg.hasTasks());

		MyTask t0 = new MyTask("t0", 5000);
		MyTask t1 = new MyTask("t1", 5000);
		MyTask t2 = new MyTask("t2", 5000);
		MyTask t3 = new MyTask("t3", 5000);

		dg.insert(t3);
		assertTrue(dg.hasTasks());

		dg.insert(t2, t3);
		dg.insert(t1, t2);
		dg.insert(t0, t1);
		
		System.out.println(dg.getGraphJobTree());

		assertNull(dg.getErrors());
		assertTrue(dg.hasNextRunnalbeDTask());
		assertEquals(t3, dg.nextRunnableDTask());

		t3.setDone(true);
		t3.setSuccess(true);

		dg.notifyDone(t3);

		assertTrue(dg.hasNextRunnalbeDTask());
		assertEquals(t2, dg.nextRunnableDTask());

		dg.notifyDone(t2);

		assertFalse(dg.hasNextRunnalbeDTask());
		assertEquals(null, dg.nextRunnableDTask());

		assertEquals(1, dg.getFailedTasks().size());
		assertEquals(2, dg.getZonbieTasks().size());

	}
	
	

	@Test
	public void testDTaskGraphTree() {
		DTaskGraph dg = new DTaskGraph("test1");

		MyTask t0 = new MyTask("t0", 5000);
		MyTask t1 = new MyTask("t1", 5000);
		MyTask t2 = new MyTask("t2", 5000);
		MyTask t3 = new MyTask("t3", 5000);
		
		MyTask t4 = new MyTask("t4", 5000);
		MyTask t5 = new MyTask("t5", 5000);
		MyTask t6 = new MyTask("t6", 5000);
		
		MyTask t7 = new MyTask("t7", 5000);
		MyTask t8 = new MyTask("t8", 5000);
		MyTask t9 = new MyTask("t9", 5000);
		MyTask t10 = new MyTask("t10", 5000);
		MyTask t11 = new MyTask("t11", 5000);
		MyTask t12 = new MyTask("t12", 5000);
		
		dg.insert(t3);

		dg.insert(t2, t3);
		dg.insert(t1, t2);
		dg.insert(t0, t1);
		
		dg.insert(t4);
		dg.insert(t5);
		dg.insert(t6, t5);
		
		dg.insert(t7,t8);
		
		dg.insert(t10,t9);
		dg.insert(t9,t7);
		dg.insert(t11,t10);
		dg.insert(t12,t10);
		
		System.out.println(dg);
		
		System.out.println(dg.getGraphJobTree());
		
	}

	@Test
	public void testMultiThreadedExecutor() throws InterruptedException, DependencyDoesNotExistException {
		MultiThreadedDTaskExecutor executor = new MultiThreadedDTaskExecutor();
		DTaskGraph dg = new DTaskGraph("test2");
		MyTask t0 = new MyTask("t0", 5000);
		MyTask t1 = new MyTask("t1", 5000);
		MyTask t2 = new MyTask("t2", 5000);
		MyTask t3 = new MyTask("t3", 5000);
		MyTask t4 = new MyTask("t4", 5000);

		dg.insert(t3);
		dg.insert(t2, t3);
		dg.insert(t1, t2);
		dg.insert(t4, t2);
		dg.insert(t4, t0);
		dg.insert(t0, t1);
		dg.insert(t1, new AbstractDTask() {

			@Override
			public void run() {
				if (3 / 0 == 0) {
					return;
				}
			}
		});

		executor.submit(dg);
		executor.waitDTaskGraphCompleted(dg);
	}

	public class MyTask extends AbstractDTask {

		private long sleepMillis;

		public MyTask(String name, long sleepMillis) {
			this.sleepMillis = sleepMillis;
			setName(name);
			setGroup("mygroup");
		}

		@Override
		public void run() {
			Uninterruptibles.sleepUninterruptibly(sleepMillis, TimeUnit.MILLISECONDS);
			isDone = true;
			isSuccess = true;
			isError = false;
			result.add(getName());
		}

		public void setDone(boolean isDone) {
			this.isDone = isDone;
		}

		public void setSuccess(boolean success) {
			isSuccess = success;
		}

		public void setError(boolean error) {
			isError = error;
		}

	}
}
