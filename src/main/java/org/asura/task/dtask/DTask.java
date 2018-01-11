package org.asura.task.dtask;

import java.io.Serializable;

public interface DTask extends Runnable, Serializable {

	public boolean isDone();

	public String getName();

	public void setName(String name);

	public void setContext(DTaskContext context);

	public void setGroup(String group);

	public String getGroup();

	public DTaskContext getContext();

	public String getDesc();

	public void setDesc(String desc);

	public void setDependency(String[] dependencies);

	public String[] getDependency();

	public void setDtaskMessager(DTaskMessager messager);

	public DTaskMessager getDtaskMessager();

	public String getSuccessMessage();

	public String getErrorMessage();

	public String getDescripter();

	public boolean isSuccess();

	public boolean isError();

	public enum STATE {
		START("started"), STOP("stopped"), ERROR("error"), SUCCESS("succeed");

		private String desc;

		private STATE(String desc) {
			this.desc = desc;
		}

		public String toString() {
			return this.desc;
		}
	}

}
