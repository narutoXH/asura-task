package org.asura.task.dtask.imp;

import org.asura.task.dtask.DTask;
import org.asura.task.dtask.DTaskMessager;

public class DefaultDTaskMessager implements DTaskMessager {
	
	private static final long serialVersionUID = 1L;

	@Override
	public void debug(DTask t, String message) {
		output(t, message, null);
	}

	@Override
	public void debug(DTask t, String message, Throwable e) {
		output(t, message, e);
	}

	@Override
	public void info(DTask t, String message) {
		output(t, message, null);
	}

	@Override
	public void info(DTask t, String message, Throwable e) {
		output(t, message, e);
	}

	@Override
	public void warn(DTask t, String message) {
		output(t, message, null);
	}

	@Override
	public void warn(DTask t, String message, Throwable e) {
		output(t, message, e);
	}

	@Override
	public void error(DTask t, String message) {
		output(t, message, null);
	}

	@Override
	public void error(DTask t, String message, Throwable e) {
		output(t, message, e);
	}

	private void output(DTask t, Object message, Throwable e) {
		System.out.println(t.getGroup() + "@@@" + t.getName() + "\t" + message + (e == null ? "" : e.getCause()));
	}

}
