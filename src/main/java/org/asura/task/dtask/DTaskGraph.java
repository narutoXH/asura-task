package org.asura.task.dtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.asura.task.exception.DependencyDoesNotExistException;

import com.google.common.collect.ArrayListMultimap;

public class DTaskGraph {

	private final Set<DTask> tasks = new HashSet<>();

	private final ArrayListMultimap<DTask, DTask> dependencies = ArrayListMultimap.create();

	private final ArrayListMultimap<DTask, DTask> revertDependencies = ArrayListMultimap.create();

	private final Set<DTask> failedTasks = new HashSet<>();

	private final Set<DTask> zombieTasks = new HashSet<>();

	private Map<DTask, Throwable> errors = null;

	private final Set<TaskTreeNode> taskTreeSet = new HashSet<>();

	private String name;

	public DTaskGraph(String name) {
		this.name = name;
	}

	public enum Status {
		COMPLTED_ALL_TASKS, ERRORS, INVALID_DEPENDENCIES
	}

	public synchronized Status status() {
		if (tasks.size() == 0) {
			return Status.COMPLTED_ALL_TASKS;
		}

		if (errors != null) {
			return Status.ERRORS;
		}

		if (tasks.size() > 0) {
			return Status.INVALID_DEPENDENCIES;
		}

		throw new RuntimeException("unknown state");
	}

	public synchronized Map<DTask, Throwable> getErrors() {
		return errors;
	}

	private synchronized DTask peekNextRunnableDTask() {
		for (DTask t : tasks) {
			if (dependencies.containsKey(t)) {
				List<DTask> v = dependencies.get(t);
				if (v.isEmpty()) {
					return t;
				}
			} else {
				return t;
			}
		}
		return null;
	}

	public synchronized boolean hasNextRunnalbeDTask() {
		return peekNextRunnableDTask() != null;
	}
	
	public Set<String> getAllTaskNames(){
		Set<String> result=new HashSet<>();
		for(DTask task:tasks){
			result.add(task.getName());
		}
		return result;
	}

	public synchronized boolean hasTasks() {
		return tasks.size() > 0;
	}

	public synchronized void insert(DTask task) {
		if (task == null) {
			throw new RuntimeException("task should not be null");
		}
		tasks.add(task);
		if (!isTreeNodeExists(task.getGroup() + ":" + task.getName())) {
			taskTreeSet.add(new TaskTreeNode(task.getGroup() + ":" + task.getName()));
		}
	}

	private boolean isTreeNodeExists(String name) {
		for (TaskTreeNode node : taskTreeSet) {
			if (getTreeNodeWithName(node, name) != null) {
				return true;
			}
		}

		return false;
	}

	private TaskTreeNode getTreeNodeWithName(TaskTreeNode node, String name) {
		if (node.getNodeName().equals(name)) {
			return node;
		} else {
			for (TaskTreeNode nd : node.getSubNodes()) {
				TaskTreeNode targetNode = getTreeNodeWithName(nd, name);
				if (targetNode != null) {
					return targetNode;
				}
			}
			return null;
		}
	}

	private TaskTreeNode getTreeNodeWithName(String name) {
		for (TaskTreeNode node : taskTreeSet) {
			if (node.equals(name)) {
				return node;
			}
			TaskTreeNode nd = getTreeNodeWithName(node, name);
			if (nd != null) {
				return nd;
			}
		}
		return null;
	}

	public synchronized void insert(DTask task, DTask dependency) {
		if (task == null) {
			throw new RuntimeException("task should not be null");
		}
		if (dependency == null) {
			throw new RuntimeException("dependency task should not be null");
		}
		tasks.add(task);
		tasks.add(dependency);
		dependencies.put(task, dependency);
		revertDependencies.put(dependency, task);
		TaskTreeNode dnode = new TaskTreeNode(dependency.getGroup() + ":" + dependency.getName());
		TaskTreeNode tnode = new TaskTreeNode(task.getGroup() + ":" + task.getName());
		for (TaskTreeNode nd : taskTreeSet) {
			if (nd.getNodeName().equals(tnode.getNodeName())) {
				tnode = nd;
				taskTreeSet.remove(nd);
				break;
			}
		}
		if (!isTreeNodeExists(dnode.getNodeName())) {
			dnode.addSubNode(tnode);
			taskTreeSet.add(dnode);
		} else {
			TaskTreeNode targetNode = getTreeNodeWithName(dnode.getNodeName());
			if (targetNode != null) {
				targetNode.addSubNode(tnode);
			}
		}
	}

	public synchronized void insert(DTask task, Set<DTask> dependencies) {
		tasks.add(task);
		tasks.addAll(dependencies);
		this.dependencies.putAll(task, dependencies);
		for (DTask t : dependencies) {
			this.revertDependencies.put(t, task);
		}
	}

	public synchronized List<DTask> getDependency(DTask task) {
		return dependencies.get(task);
	}

	public synchronized List<DTask> getRevertDependencies(DTask task) {
		return revertDependencies.get(task);
	}

	public synchronized Set<DTask> getFailedTasks() {
		return failedTasks;
	}

	public synchronized Set<DTask> getZonbieTasks() {
		return zombieTasks;
	}

	public synchronized DTask nextRunnableDTask() {
		DTask t = peekNextRunnableDTask();
		tasks.remove(t);
		return t;
	}

	public synchronized void notifyDone(DTask task) {
		if (task.isDone() && task.isSuccess()) {
			while (dependencies.values().contains(task)) {
				dependencies.values().remove(task);
			}
		} else {
			failedTasks.add(task);
			calculateZombieTasks(task);
		}
	}

	private synchronized void calculateZombieTasks(DTask task) {
		if (revertDependencies.get(task).size() == 0) {
			return;
		} else {
			for (DTask t : revertDependencies.get(task)) {
				zombieTasks.add(t);
				calculateZombieTasks(t);
			}
		}
	}

	public synchronized void notifyError(DTask t, Throwable error) {
		if (errors == null) {
			errors = new HashMap<>();
		}
		errors.put(t, error);
		failedTasks.add(t);
		calculateZombieTasks(t);
	}

	public synchronized int numTasks() {
		return tasks.size();
	}

	public synchronized int numZombieTasks() {
		return zombieTasks.size();
	}

	public synchronized int numFailedTasks() {
		return failedTasks.size();
	}

	public void verifyValidGraph() throws DependencyDoesNotExistException {
		for (DTask t : dependencies.values()) {
			if (!tasks.contains(t)) {
				throw new DependencyDoesNotExistException(t);
			}
		}

		for (DTask t : revertDependencies.values()) {
			if (!tasks.contains(t)) {
				throw new DependencyDoesNotExistException(t);
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGraphJobTree() {
		StringBuffer result = new StringBuffer("\n");
		for (TaskTreeNode node : taskTreeSet) {
			for (String rt : getGraphSubTree(node)) {
				result.append(rt);
			}
		}
		return result.toString();
	}

	private List<String> getGraphSubTree(TaskTreeNode node) {
		List<String> result = new ArrayList<>();
		result.add(node.getNodeName() + "\n");
		for (TaskTreeNode nd : node.getSubNodes()) {
			for (String rt : getGraphSubTree(nd)) {
				result.add("\t" + rt);
			}
		}
		return result;
	}

	private static class TaskTreeNode {
		private String nodeName;
		private Set<TaskTreeNode> children = new HashSet<>();

		public TaskTreeNode(String nodeName) {
			this.nodeName = nodeName;
		}

		public String getNodeName() {
			return nodeName;
		}

		public void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}

		public void addSubNode(TaskTreeNode node) {
			children.add(node);
		}

		public Set<TaskTreeNode> getSubNodes() {
			return this.children;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((nodeName == null) ? 0 : nodeName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TaskTreeNode other = (TaskTreeNode) obj;
			if (nodeName == null) {
				if (other.nodeName != null)
					return false;
			} else if (!nodeName.equals(other.nodeName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TaskTreeNode [nodeName=" + nodeName + ", children=" + StringUtils.join(children, ";") + "]";
		}

	}

	@Override
	public String toString() {
		return "DTaskGraph [\n" + "name=" + name + ",\n" + "tree info=[\n" + getGraphJobTree() + "],\n" + "task size="
				+ tasks.size() + ",\n" + "error task size=" + failedTasks.size() + ",\n" + "zombie task size="
				+ zombieTasks.size() + ",\n"+"task detail=["+getTaskDetail()+"],\n"+"error task detail=["+getErrorTaskDetail()+"],\n"+"zombie task detail=["+getZombieTaskDetail()+"]\n]\n";
	}

	private String getTaskDetail() {
		StringBuffer result=new StringBuffer();
		for(DTask task:tasks){
			result.append(task.getName()+",");
		}
		if(result.length()>0){
			result.deleteCharAt(result.length()-1);
		}
		return result.toString();
	}

	private String getErrorTaskDetail() {
		StringBuffer result=new StringBuffer();
		for(DTask task:failedTasks){
			result.append(task.getName()+",");
		}
		if(result.length()>0){
			result.deleteCharAt(result.length()-1);
		}
		return result.toString();
	}

	private String getZombieTaskDetail() {
		StringBuffer result=new StringBuffer();
		for(DTask task:zombieTasks){
			result.append(task.getName()+",");
		}
		if(result.length()>0){
			result.deleteCharAt(result.length()-1);
		}
		return result.toString();
	}

}
