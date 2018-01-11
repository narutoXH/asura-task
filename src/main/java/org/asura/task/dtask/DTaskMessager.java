package org.asura.task.dtask;

import java.io.Serializable;

public interface DTaskMessager extends Serializable {

	public void debug(DTask t, String message);

	public void debug(DTask t, String message, Throwable e);

	public void info(DTask t, String message);

	public void info(DTask t, String message, Throwable e);

	public void warn(DTask t, String message);

	public void warn(DTask t, String message, Throwable e);

	public void error(DTask t, String message);

	public void error(DTask t, String message, Throwable e);

}
