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
package org.pac4j.play;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.context.session.SessionStore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import play.mvc.Http.Context;
import play.mvc.Http.Request;

/**
 * Unit test cases for PlayWebContext.java class
 * 
 * @author furkan yavuz
 * @since 2.1.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class, Request.class, PlayWebContext.class })
public class PlayWebContextTest {

	private PlayWebContext playWebContext;
	private Request request;

	@Before
	public void setUp() throws Exception {
		request = mock(Request.class);
		Context context = mock(Context.class);
		doReturn(request).when(context).request();
		SessionStore sessionStore = mock(SessionStore.class);
		playWebContext = new PlayWebContext(context, sessionStore);
	}

	@Test
	public final void testGetRequestParameter() {
		// given
		String expected = "test";
		Map<String, String[]> urlParameters = new HashMap<>();
		urlParameters.put(expected, new String[] { expected });
		doReturn(urlParameters).when(request).queryString();

		// when
		String parameter = playWebContext.getRequestParameter(expected);

		// then
		assertEquals("Parameter must be equal to expected.", expected, parameter);
	}

	@Test
	public final void testGetServerPort() {
		// given
		String host = "localhost:";
		String port = "8080";
		doReturn(host + port).when(request).host();
		
		// when
		int result = playWebContext.getServerPort();

		// then
		assertEquals("Result value must be equal to port.", port, String.valueOf(result));
		
	}

	@Test
	public final void testGetSchemeSecure() {
		// given
		doReturn(true).when(request).secure();
		
		// when
		String result = playWebContext.getScheme();
		
		// then
		assertEquals("Result value must be equal to https", "https", result);
	}

	@Test
	public final void testGetSchemeNotSecure() {
		// given
		doReturn(false).when(request).secure();
		
		// when
		String result = playWebContext.getScheme();
		
		// then
		assertEquals("Result value must be equal to http", "http", result);
	}
}
