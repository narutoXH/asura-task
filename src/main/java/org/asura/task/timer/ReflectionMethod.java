package org.asura.task.timer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionMethod {
	private String className;
	private String methodName;
	private Object instance;
	private Object[] params;
	private Class<?>[] classes;

	public String getClassName() {
		return this.className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public void run() {
		try {
			Object obj = null;
			Class<?> cl = null;
			if (this.instance == null) {
				cl = Class.forName(this.className);
				obj = cl.newInstance();
			} else {
				cl = this.instance.getClass();
				obj = this.instance;
			}
			if (this.classes == null) {
				Method mt = cl.getMethod(this.methodName, new Class[0]);
				mt.invoke(obj, new Object[0]);
			} else {
				Method mt = cl.getMethod(this.methodName, this.classes);
				mt.invoke(obj, this.params);
			}
			FlexTimer.logger.info(
					"The method '" + this.methodName + "' in class '" + this.className + "' is called successfully");
		} catch (ClassNotFoundException e) {
			FlexTimer.logger.error("Load class '" + this.className + "' faield.\n" + getExceptionContent(e));
		} catch (InstantiationException e) {
			FlexTimer.logger.error("Instance class '" + this.className + "' faield.\n" + getExceptionContent(e));
		} catch (IllegalAccessException e) {
			FlexTimer.logger
					.error("Access constructor of class '" + this.className + "'  faield.\n" + getExceptionContent(e));
		} catch (SecurityException e) {
			FlexTimer.logger.error("Throw method security exception at class '" + this.className + "' method '"
					+ this.methodName + "'.\n" + getExceptionContent(e));
		} catch (NoSuchMethodException e) {
			FlexTimer.logger.error("No '" + this.methodName + "' method in class '" + this.className + "'  faield.\n"
					+ getExceptionContent(e));
		} catch (IllegalArgumentException e) {
			FlexTimer.logger.error("Illegal argument for class '" + this.className + "' method '" + this.methodName
					+ "'.\n" + getExceptionContent(e));
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			FlexTimer.logger.error("Throw InvocationTarget exception when invoke  class '" + this.className
					+ "' method '" + this.methodName + "'.\n" + getExceptionContent(e));
		}
	}

	public Object getInstance() {
		return this.instance;
	}

	public void setInstance(Object instance) {
		this.instance = instance;
	}

	public Object[] getParams() {
		return this.params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public Class<?>[] getClasses() {
		return this.classes;
	}

	public void setClasses(Class<?>[] classes) {
		this.classes = classes;
	}

	private String getExceptionContent(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String content = sw.toString();
		try {
			sw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return content;
	}
}
