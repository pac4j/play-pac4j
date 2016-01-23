/*
  Copyright 2012 - 2015 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.play.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author furkan yavuz
 * @since 2.1.0
 */
public class AbstractConfigActionTest {
	
	private AbstractConfigActionStub abstractConfigAction;

	@Before
	public void setUp() throws Exception {
		abstractConfigAction = new AbstractConfigActionStub();
	}

	@Test
	public final void testGetStringParam() throws Throwable {
		// given
		InvocationHandler invocationHandler = mock(InvocationHandler.class);
		Method method = mock(Method.class);
		String value = "value";
		doReturn(value).when(invocationHandler).invoke(null, method, null);
		
		// when
		String result = abstractConfigAction.getStringParam(invocationHandler, method, null);
		
		// then
		assertEquals("Result must be equal to " + value, value, result);
	}
	
	@Test
	public final void testGetStringParamDefaultValue() throws Throwable {
		// given
		InvocationHandler invocationHandler = mock(InvocationHandler.class);
		Method method = mock(Method.class);
		String defaultValue = "DEFAULT";
		
		// when
		String result = abstractConfigAction.getStringParam(invocationHandler, method, defaultValue);
		
		// then
		assertEquals("Result must be equal to " + defaultValue, defaultValue, result);
	}

	@Test
	public final void testGetBooleanParam() throws Throwable {
		// given
		InvocationHandler invocationHandler = mock(InvocationHandler.class);
		Method method = mock(Method.class);
		boolean value = true;
		doReturn(value).when(invocationHandler).invoke(null, method, null);
		
		// when
		boolean result = abstractConfigAction.getBooleanParam(invocationHandler, method, false);
		
		// then
		assertTrue("Result must be equal to true", result);
	}
	
	@Test
	public final void testGetBooleanParamDefaultValue() throws Throwable {
		// given
		InvocationHandler invocationHandler = mock(InvocationHandler.class);
		Method method = mock(Method.class);
		
		// when
		boolean result = abstractConfigAction.getBooleanParam(invocationHandler, method, true);
		
		// then
		assertTrue("Result must be equal to true", result);
	}

}
