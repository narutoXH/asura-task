package org.asura.task.annotations.parser;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;

public class AnnotationParserFactoryBuilder {

	public static class Builder<F extends AnnotationParserFactory> {

		private String[] packages;

		public Builder(String... packages) {
			this.packages = packages;
		}

		public F build(Class<? extends Annotation> atype, F factory) throws Exception {
			Set<Class<?>> results = new HashSet<>();
			for (String pkg : packages) {
				Reflections reflections = new Reflections(pkg);
				Set<Class<?>> rs = reflections.getTypesAnnotatedWith(atype);
				// Set<Class<?>> rs = ClassScanner.scan(pkg, atype);
				if (rs != null && !rs.isEmpty()) {
					results.addAll(rs);
				}
			}

			factory.init(packages, results);

			return factory;
		}

	}
}
