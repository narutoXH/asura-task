package org.asura.task.annotations.task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.lang.StringUtils;
import org.asura.task.annotations.parser.AnnotationParserFactory;
import org.asura.task.dtask.DTask;
import org.asura.task.dtask.DTaskContext;
import org.asura.task.dtask.DTaskGraph;
import org.asura.task.dtask.DTaskMessager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskAnnotationParserFactory implements AnnotationParserFactory {

	private static final Logger logger = LoggerFactory.getLogger(TaskAnnotationParserFactory.class);

	private static final Map<String, Map<String, Set<String>>> dMap = new HashMap<>();

	private static final Map<String, Map<String, DTask>> taskMap = new HashMap<>();

	@Override
	public void init(String[] packages, Set<Class<?>> classes) throws Exception {
		logger.info("begin to init " + StringUtils.join(packages, ","));
		for (Class<?> cls : classes) {
			Task taskDef = cls.getAnnotation(Task.class);
			DTask task = (DTask) ConstructorUtils.invokeConstructor(cls, null);
			task.setDesc(taskDef.desc());
			task.setName(taskDef.name());
			task.setDependency(taskDef.dependency());
			task.setGroup(taskDef.group());
			if (taskMap.get(taskDef.group()) == null) {
				taskMap.put(taskDef.group(), new HashMap<String, DTask>());
			}
			if (taskMap.get(taskDef.group()).get(taskDef.name()) != null) {
				throw new RuntimeException(
						"duplicate task name exists in same group" + taskDef.group() + ":" + taskDef.name());
			}
			taskMap.get(taskDef.group()).put(taskDef.name(), task);
			if (dMap.get(taskDef.group()) == null) {
				dMap.put(taskDef.group(), new HashMap<String, Set<String>>());
			}
			if (dMap.get(taskDef.group()).get(taskDef.name()) == null) {
				dMap.get(taskDef.group()).put(taskDef.name(), new HashSet<String>());
			}
			for (String dependency : taskDef.dependency()) {
				if (StringUtils.isNotBlank(dependency)) {
					dMap.get(taskDef.group()).get(taskDef.name()).add(dependency);
				}
			}

		}

	}

	public Set<String> getTaskGroups() {
		return dMap.keySet();
	}

	public Set<String> getTaskNames(String group) {
		if (dMap.containsKey(group)) {
			return dMap.get(group).keySet();
		} else {
			return new HashSet<>();
		}
	}

	public Map<String, Map<String, DTask>> getTaskMap() {
		return taskMap;
	}

	public DTaskGraph getTaskGraph(String group, DTaskMessager messager, DTaskContext context) {
		DTaskGraph graph = new DTaskGraph(group);
		if (dMap.get(group) == null) {
			throw new RuntimeException("group doesn't exist-" + group);
		}
		for (Entry<String, Set<String>> entry : dMap.get(group).entrySet()) {
			DTask db = taskMap.get(group).get(entry.getKey());
			db.setDtaskMessager(messager);
			db.setContext(context);
			if (entry.getValue().size() > 0) {
				for (String dependency : entry.getValue()) {
					DTask dd = taskMap.get(group).get(dependency);
					dd.setDtaskMessager(messager);
					dd.setContext(context);
					graph.insert(db, dd);
				}
			} else {
				graph.insert(db);
			}
		}
		return graph;
	}

	public DTaskGraph getTaskGraph(String group, String taskName, DTaskMessager messager, DTaskContext context,
			boolean isStandAlone) {
		DTaskGraph graph = new DTaskGraph(group);
		if (dMap.get(group) == null) {
			throw new RuntimeException("group doesn't exist-" + group);
		}
		if (dMap.get(group).get(taskName) == null) {
			throw new RuntimeException("taskname doesn't exist-" + group + ":" + taskName);
		}
		DTask db = taskMap.get(group).get(taskName);
		db.setDtaskMessager(messager);
		db.setContext(context);
		if (isStandAlone) {
			graph.insert(db);
		} else {
			if (dMap.get(group).get(taskName).size() > 0) {
				for (String dependency : dMap.get(group).get(taskName)) {
					DTask dd = taskMap.get(group).get(dependency);
					dd.setDtaskMessager(messager);
					dd.setContext(context);
					graph.insert(db, dd);
				}
			} else {
				graph.insert(db);
			}
		}

		return graph;
	}

}
