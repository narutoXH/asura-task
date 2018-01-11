package org.asura.task.timer;

public class Task {
	private String id;
	private ReflectionMethod method;
	private long timerInterval;
	private Object parameter;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getTimerInterval() {
		return this.timerInterval;
	}

	public void setTimerInterval(long timerInterval) {
		this.timerInterval = timerInterval;
	}

	public Object getParameter() {
		return this.parameter;
	}

	public void setParameter(Object parameter) {
		this.parameter = parameter;
	}

	public ReflectionMethod getMethod() {
		return this.method;
	}

	public void setMethod(ReflectionMethod method) {
		this.method = method;
	}
}
