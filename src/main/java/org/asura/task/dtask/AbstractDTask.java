package org.asura.task.dtask;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.asura.task.dtask.imp.DefaultDTaskMessager;

public abstract class AbstractDTask implements DTask {

	private static final long serialVersionUID = 1L;

	protected String name;
	protected String descriptor;

	protected boolean isDone;
	protected boolean isSuccess;
	protected boolean isError;

	protected String successMessage;
	protected String errorMessage;

	protected DTaskMessager messager;

	protected Set<String> dependency = new HashSet<>();

	protected DTaskContext context = new DTaskContext();

	protected String group = "";

	@Override
	public void setGroup(String group) {
		if (StringUtils.isNotBlank(group)) {
			this.group = group;
		}
	}

	@Override
	public String getGroup() {
		return this.group;
	}

	@Override
	public void setContext(DTaskContext context) {
		if (context != null) {
			this.context = context;
		}
	}

	@Override
	public DTaskContext getContext() {
		return this.context;
	}

	@Override
	public void setDependency(String[] dependencies) {
		if (dependencies != null) {
			for (String dp : dependencies) {
				if (StringUtils.isNotBlank(dp)) {
					dependency.add(dp);
				}
			}
		}
	}

	@Override
	public String[] getDependency() {
		return this.dependency.toArray(new String[] {});
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDesc() {
		return this.descriptor;
	}

	@Override
	public void setDesc(String desc) {
		this.descriptor = desc;
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setDtaskMessager(DTaskMessager messager) {
		if (messager != null) {
			this.messager = messager;
		} else {
			this.messager = new DefaultDTaskMessager();
		}
	}

	@Override
	public DTaskMessager getDtaskMessager() {
		return this.messager;
	}

	@Override
	public String getSuccessMessage() {
		return successMessage;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public String getDescripter() {
		return descriptor;
	}

	@Override
	public boolean isSuccess() {
		return isSuccess;
	}

	@Override
	public boolean isError() {
		return isError;
	}

}
