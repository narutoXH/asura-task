package org.asura.task.annotations.handler.rchandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.lang.StringUtils;
import org.asura.task.annotations.parser.AnnotationParserFactory;
import org.asura.task.handler.rchandler.ResChainHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RCHandlerAnnotationParserFactory implements AnnotationParserFactory {

	private static final Logger logger = LoggerFactory.getLogger(RCHandlerAnnotationParserFactory.class);

	private Map<String, Map<String, ResChainHandler>> handlersMap = new HashMap<>();

	private Map<String, Map<String, List<String>>> revertDependencyMap = new HashMap<>();

	private Map<String, ResChainHandler> calculatedGroupHandlers = new HashMap<>();

	public ResChainHandler getResChainHandler(String group) {
		return calculatedGroupHandlers.get(group);
	}

	public ResChainHandler calculatedGroupHandlers(String group, String name) {
		return handlersMap.get(group).get(name);
	}

	public String getGroupHandlerTreeInfo() {
		StringBuffer result = new StringBuffer("\n");
		for (Entry<String, Map<String, List<String>>> entry : revertDependencyMap.entrySet()) {
			result.append(entry.getKey() + "\n");
			Map<String, List<String>> kvMap=revertDependencyMap.get(entry.getKey());
			for(String handler:kvMap.get("")){
				for(String rs:getHandlerTreeInfo(handler, kvMap)){
					result.append(rs);
				}
			}
			
		}
		return result.toString();
	}
	
	public List<String> getHandlerTreeInfo(String handler, Map<String, List<String>> kvMap){
		List<String> result=new ArrayList<>();
		result.add("\t"+handler+"\n");
		if(kvMap.containsKey(handler)){
			for(String dhander:kvMap.get(handler)){
				for(String rs:getHandlerTreeInfo(dhander, kvMap)){
					result.add("\t"+rs);
				}
			}
		}
		return result;
	}

	public String getGroupHandlerLineInfo() {
		StringBuffer result = new StringBuffer("\n");
		for (Entry<String, ResChainHandler> entry : calculatedGroupHandlers.entrySet()) {
			result.append(entry.getKey() + ":");
			ResChainHandler handler = entry.getValue();
			result.append(handler.getName());
			while (handler.getSuccesor() != null) {
				handler = handler.getSuccesor();
				result.append("," + handler.getName());
			}
			result.append(")\n");
		}
		return result.toString();
	}

	@Override
	public void init(String[] packages, Set<Class<?>> classes) throws Exception {
		logger.info("begin to init " + StringUtils.join(packages, ","));
		Set<String> uniqueNameSet = new HashSet<>();
		int count = 1;
		for (Class<?> cls : classes) {
			RCHandler rchandler = cls.getAnnotation(RCHandler.class);
			ResChainHandler handler = (ResChainHandler) ConstructorUtils.invokeConstructor(cls, null);
			handler.setName(rchandler.name());
			handler.setDecription(rchandler.desc());
			if (uniqueNameSet.contains(rchandler.group() + "-" + rchandler.name())) {
				logger.error("unique group + name exsits in rc handlers" + rchandler.group() + "-" + rchandler.name());
				throw new RuntimeException("unique group + name exsits in rc handlers");
			}
			if (revertDependencyMap.get(rchandler.group()) == null) {
				revertDependencyMap.put(rchandler.group(), new HashMap<String, List<String>>());
			}

			if (revertDependencyMap.get(rchandler.group()).get(rchandler.prehandler()) == null) {
				revertDependencyMap.get(rchandler.group()).put(rchandler.prehandler(), new ArrayList<String>());
			}
			revertDependencyMap.get(rchandler.group()).get(rchandler.prehandler()).add(rchandler.name());
			if (handlersMap.get(rchandler.group()) == null) {
				handlersMap.put(rchandler.group(), new HashMap<String, ResChainHandler>());
			}
			handlersMap.get(rchandler.group()).put(rchandler.name(), handler);

			logger.debug((count++) + ":handler info " + getRchandlerInfo(rchandler));
		}

		// clearRevertDepencdencyMap();
		validateHandlers();
		constructHandlers();
		logger.info("end to init " + StringUtils.join(packages, ","));
		logger.info(getGroupHandlerTreeInfo());
		logger.info(getGroupHandlerLineInfo());
	}

	/*private void clearRevertDepencdencyMap() {
		for (String group : revertDependencyMap.keySet()) {
			Map<String, List<String>> kvMap = revertDependencyMap.get(group);
			Set<String> keys = new HashSet<>();
			for (String key : kvMap.keySet()) {
				if (!StringUtils.isBlank(key)) {
					keys.add(key);
				}
			}
			if (kvMap.get("") != null) {
				Iterator<String> it = kvMap.get("").iterator();
				while (it.hasNext()) {
					String key = it.next();
					if (keys.contains(key)) {
						it.remove();
					}
				}
			}
		}
	}*/

	private String getRchandlerInfo(RCHandler handler) {
		return "(group:" + handler.group() + "@name:" + handler.name() + "@prehandler:" + handler.prehandler() + ")";
	}

	private List<String> shrinkLineDepends(List<String> handlers, Set<String> dependsHandlerSet,
			Map<String, List<String>> kvMap) {
		List<String> result = new ArrayList<>();
		Set<String> dhandlers = new HashSet<>();
		for (String handler : handlers) {
			if (dependsHandlerSet.contains(handler)) {
				dhandlers.add(handler);
			} else {
				result.add(handler);
			}
		}
		for (String handler : dhandlers) {
			result.add(handler);
			result.addAll(shrinkLineDepends(kvMap.get(handler), dependsHandlerSet, kvMap));
		}
		return result;
	}

	private void constructHandlers() {
		for (String group : revertDependencyMap.keySet()) {
			Map<String, List<String>> kvMap = revertDependencyMap.get(group);
			Map<String, ResChainHandler> hdlsMap = handlersMap.get(group);

			Set<String> dependsHandlerSet = kvMap.keySet();

			List<String> result = shrinkLineDepends(kvMap.get(""), dependsHandlerSet, kvMap);

			logger.info("shink line result:" + group + ":" + StringUtils.join(result, ","));
			ResChainHandler firstHandler = null;
			for (String handler : result) {
				if (firstHandler == null) {
					firstHandler = hdlsMap.get(handler);
				} else {
					ResChainHandler guardHandler = firstHandler;
					while (guardHandler.getSuccesor() != null) {
						guardHandler = guardHandler.getSuccesor();
					}
					guardHandler.setSuccesor(hdlsMap.get(handler));
				}
			}
			calculatedGroupHandlers.put(group, firstHandler);
		}
	}

	private void validateHandlers() {
		for (String group : revertDependencyMap.keySet()) {
			Map<String, List<String>> kvMap = revertDependencyMap.get(group);
			Map<String, ResChainHandler> hdlsMap = handlersMap.get(group);
			for (Entry<String, List<String>> entry : kvMap.entrySet()) {
				if (!StringUtils.isEmpty(entry.getKey())) {
					if (hdlsMap.get(entry.getKey()) == null) {
						RuntimeException ex = new RuntimeException(entry.getKey() + " doesn't match any handler");
						logger.error("exeption occured", ex);
						throw ex;
					}
				}
				for (String successor : entry.getValue()) {
					if (hdlsMap.get(successor) == null) {
						RuntimeException ex = new RuntimeException(entry.getKey() + " doesn't match any handler");
						logger.error("exeption occured", ex);
						throw ex;
					}
				}
			}
		}
	}

}
