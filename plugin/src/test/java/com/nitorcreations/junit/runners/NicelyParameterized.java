/**
 * Copyright 2012 Nitor Creations Oy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nitorcreations.junit.runners;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

public class NicelyParameterized extends Parameterized {

	private class TestClassRunnerForParameters extends BlockJUnit4ClassRunner {
		private final int fParameterSetNumber;

		private final List<Object[]> fParameterList;

		private String name;

		TestClassRunnerForParameters(Class<?> type,
				List<Object[]> parameterList, int i) throws InitializationError {
			super(type);
			fParameterList = parameterList;
			fParameterSetNumber = i;
		}

		@Override
		public Object createTest() throws Exception {
			return getTestClass().getOnlyConstructor().newInstance(
					computeParams());
		}

		private Object[] computeParams() throws Exception {
			try {
				return fParameterList.get(fParameterSetNumber);
			} catch (ClassCastException e) {
				throw new Exception(String.format(
						"%s.%s() must return a Collection of arrays.",
						getTestClass().getName(),
						getParametersMethod(getTestClass()).getName()));
			}
		}

		@Override
		protected String getName() {
			if (name == null) {
				StringBuilder sb = new StringBuilder();
				boolean first = true;
				for (Object parameter : fParameterList.get(fParameterSetNumber)) {
					if (first) {
						first = false;
					} else {
						sb.append(", ");
					}
					if (parameter instanceof String) {
						parameter = '"' + (String) parameter + '"';
					}
					sb.append(parameter);
				}
				name = fParameterSetNumber + ". " + sb;
			}
			return name;
		}

		@Override
		protected String testName(final FrameworkMethod method) {
			/*
			 * \u0000 and anything after "accidentally" gets stripped out in the
			 * eclipse junit view but nevertheless avoids the test name from
			 * clashing with other tests (which causes the junit view to lose
			 * the results for all but one of the parameter sets)
			 */
			return method.getName() + " \u0000[" + name + "] "
					+ getTestClass().getName();
		}

		@Override
		protected void validateConstructor(List<Throwable> errors) {
			validateOnlyOneConstructor(errors);
		}

		@Override
		protected Statement classBlock(RunNotifier notifier) {
			return childrenInvoker(notifier);
		}
	}

	private final ArrayList<Runner> runners = new ArrayList<Runner>();

	/**
	 * Only called reflectively. Do not use programmatically.
	 */
	public NicelyParameterized(Class<?> klass) throws Throwable {
		super(klass);
		List<Object[]> parametersList = getParametersList(getTestClass());
		for (int i = 0; i < parametersList.size(); i++)
			runners.add(new TestClassRunnerForParameters(getTestClass()
					.getJavaClass(), parametersList, i));
	}

	@Override
	protected List<Runner> getChildren() {
		return runners;
	}

	@SuppressWarnings("unchecked")
	private List<Object[]> getParametersList(TestClass klass) throws Throwable {
		return (List<Object[]>) getParametersMethod(klass).invokeExplosively(
				null);
	}

	FrameworkMethod getParametersMethod(TestClass testClass) throws Exception {
		List<FrameworkMethod> methods = testClass
				.getAnnotatedMethods(Parameters.class);
		if (methods.isEmpty()) {
			throw new RuntimeException("No @Parameters method in class "
					+ testClass.getName());
		}
		if (methods.size() > 1) {
			throw new RuntimeException(
					"Multiple methods annotated with @Parameters. Only one method should be thus annotated per class.");
		}
		FrameworkMethod method = methods.get(0);
		int modifiers = method.getMethod().getModifiers();
		if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
			throw new RuntimeException("@Parameters method " + method.getName()
					+ " must be declared public static in class "
					+ testClass.getName());
		}
		return method;
	}

}
