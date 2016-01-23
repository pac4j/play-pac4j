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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.session.SessionStore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Http.RequestBody;
import play.mvc.Http.Response;
import play.mvc.Http.Session;

/**
 * Unit test cases for PlayWebContext.java class
 * 
 * @author furkan yavuz
 * @since 2.1.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class, Request.class, PlayWebContext.class, SessionStore.class })
public class PlayWebContextTest {

	// Class under test
	private PlayWebContext playWebContext;
	
	private Request requestMock;
	private Response responseMock;
	private Session sessionMock;
	private Context contextMock;
	private SessionStore sessionStoreMock;
	
	private String host = "localhost";
	private String port = "8080";

	@Before
	public void setUp() throws Exception {
		sessionMock = mock(Session.class);
		requestMock = mock(Request.class);
		responseMock = mock(Response.class);
		contextMock = mock(Context.class);
		
		doReturn(sessionMock).when(contextMock).session();
		doReturn(requestMock).when(contextMock).request();
		doReturn(responseMock).when(contextMock).response();
		
		sessionStoreMock = mock(SessionStore.class);
		
		playWebContext = new PlayWebContext(contextMock, sessionStoreMock);
	}
	
	@Test
	public final void testConstructorNullSessionStore() {
		// when
		SessionStore nullSessionStore = null;
		PlayWebContext playWebContextNullSession = new PlayWebContext(contextMock, nullSessionStore);

		// then
		assertNotNull("Session store must be set in constructor.", playWebContextNullSession.getSessionStore());
	}

	@Test
	public final void testGetJavaSession() {
		// when
		Session javaSession = playWebContext.getJavaSession();

		// then
		assertEquals("JavaSession must be equal to sessionMock.", sessionMock, javaSession);
	}

	@Test
	public final void testGetJavaRequest() {
		// when
		Request javaRequest = playWebContext.getJavaRequest();

		// then
		assertEquals("JavaRequest must be equal to requestMock.", requestMock, javaRequest);
	}

	@Test
	public final void testGetJavaContext() {
		// when
		Context javaContext = playWebContext.getJavaContext();

		// then
		assertEquals("JavaContext must be equal to contextMock.", contextMock, javaContext);
	}
	
	@Test
	public final void testGetSessionStore() {
		// when
		SessionStore sessionStore = playWebContext.getSessionStore();
		
		// then
		assertEquals("SessionStore must be equal to sessionStoreMock.", sessionStoreMock, sessionStore);
	}
	
	@Test
	public final void testGetRequestHeader() {
		// given
		String requestHeader = "HEADER";
		doReturn(requestHeader).when(requestMock).getHeader(requestHeader);
		
		// when
		String header = playWebContext.getRequestHeader(requestHeader);
		
		// then
		assertEquals("Header must be equal to HEADER.", requestHeader, header);
	}
	
	@Test
	public final void testGetRequestMethod() {
		// given
		String requestMethod = "METHOD";
		doReturn(requestMethod).when(requestMock).method();
		
		// when
		String method = playWebContext.getRequestMethod();
		
		// then
		assertEquals("Method must be equal to METHOD.", requestMethod, method);
	}
	
	@Test
	public final void testGetSessionIdentifier() {
		// given
		String sessionIdentifierMock = "SESSION_IDENTIFIER";
		doReturn(sessionIdentifierMock).when(sessionStoreMock).getOrCreateSessionId(playWebContext);
		
		// when
		Object sessionIdentifier = playWebContext.getSessionIdentifier();
		
		// then
		assertEquals("SessionIdentifier must be equal to sessionIdentifierMock.", sessionIdentifierMock, sessionIdentifier);
	}
	
	@Test
	public final void testGetSessionAttribute() {
		// given
		String key = "KEY";
		Object attributeMock = mock(Object.class);
		doReturn(attributeMock).when(sessionStoreMock).get(playWebContext, key);
		
		// when
		Object sessionAttribute = playWebContext.getSessionAttribute(key);
		
		// then
		assertEquals("SessionAttribute must be equal to attributeMock.", attributeMock, sessionAttribute);
	}
	
	@Test
	public final void testSetSessionAttribute() {
		// given
		String key = "KEY";
		Object value = mock(Object.class);
		
		// when
		playWebContext.setSessionAttribute(key, value);
		
		// then
		verify(sessionStoreMock).set(playWebContext, key, value);
	}
	
	@Test
	public final void testSetResponseHeader() {
		// given
		String key = "KEY";
		String value = "VALUE";
		
		// when
		playWebContext.setResponseHeader(key, value);
		
		// then
		verify(responseMock).setHeader(key, value);
	}
	
	@Test
	public final void testGetServerName() {
		// given
		doReturn(host + ":" + port).when(requestMock).host();
		
		// when
		String result = playWebContext.getServerName();
		
		// then
		assertEquals("Result value must be equal to host.", host, String.valueOf(result));
	}
	
	@Test
	public final void testIsSecure() {
		// given
		doReturn(true).when(requestMock).secure();
		
		// when
		boolean result = playWebContext.isSecure();
		
		// then
		assertTrue("Result value must be equal to true.", result);
	}
	
	@Test
	public final void testGetFullRequestURL() {
		// given
		doReturn(true).when(requestMock).secure();
		doReturn(host).when(requestMock).host();
		doReturn(":" + port).when(requestMock).uri();
		String expectedResult = "https://" + host + ":" + port;
		
		// when
		String result = playWebContext.getFullRequestURL();
		
		// then
		assertEquals("Result value must be equal to " + expectedResult, expectedResult, result);
	}
	
	@Test
	public final void testGetRemoteAddr() {
		// given
		String remoteAddr = "REMOTE_ADDRESS";
		doReturn(remoteAddr).when(requestMock).remoteAddress();
		
		// when
		String result = playWebContext.getRemoteAddr();
		
		// then
		assertEquals("Result must be equal to REMOTE_ADDRESS.", remoteAddr, result);
	}
	
	@Test
	public final void testGetRequestAttribute() {
		// given
		contextMock.args = new HashMap<String, Object>();
		String key = "KEY";
		Object value = mock(Object.class);
		contextMock.args.put(key, value);
		
		// when
		Object result = playWebContext.getRequestAttribute(key);
		
		// then
		assertEquals("Result must be equal to value.", value, result);
	}
	
	@Test
	public final void testGetPath() {
		// given
		String path = "PATH";
		doReturn(path).when(requestMock).path();
		
		// when
		String result = playWebContext.getPath();
		
		// then
		assertEquals("Result must be equal to path.", path, result);
	}
	
	@Test
	public final void testSetRequestAttribute() {
		// given
		String key = "KEY";
		Object value = mock(Object.class);
		contextMock.args = new HashMap<String, Object>();
		contextMock.args.put(key, value);
		
		// when
		playWebContext.setRequestAttribute(key, value);
		
		// then
		assertTrue(contextMock.args.containsValue(value));
	}

	@Test
	public final void testGetRequestParameter() {
		// given
		String expected = "test";
		Map<String, String[]> urlParameters = new HashMap<>();
		urlParameters.put(expected, new String[] { expected });
		doReturn(urlParameters).when(requestMock).queryString();

		// when
		String parameter = playWebContext.getRequestParameter(expected);

		// then
		assertEquals("Parameter must be equal to expected.", expected, parameter);
	}

	@Test
	public final void testGetRequestParameters() {
		// given
		String key = "KEY";
		Map<String, String[]> formParameters = new HashMap<>();
		formParameters.put(key, new String[] { key });
		
		RequestBody requestBodyMock = mock(RequestBody.class);
		doReturn(requestBodyMock).when(requestMock).body();
		doReturn(formParameters).when(requestBodyMock).asFormUrlEncoded();

		// when
		Map<String, String[]> requestParameters = playWebContext.getRequestParameters();

		// then
		assertTrue("RequestParameters must contains " + key, requestParameters.containsKey(key));
	}
	
	@Test
	public void testGetRequestCookies() {
		// given
		Http.Cookie httpCookie = new Http.Cookie("name", "value", Integer.MAX_VALUE, "path", "domain", true, true);
		Http.Cookies httpCookies = new CookiesStub(httpCookie);
		doReturn(httpCookies).when(requestMock).cookies();
		
		// when
		Collection<Cookie> cookies = playWebContext.getRequestCookies();
		assertNotNull("Cookies must be initialized.", cookies);
		assertTrue("Cookies must have 1 element.", cookies.size() == 1);
		
		Cookie cookie = (Cookie) cookies.toArray()[0];
		
		// then
		assertEquals("Domain must be equal to " + httpCookie.domain(), httpCookie.domain(), cookie.getDomain());
		assertEquals("HttpOnly must be equal to " + httpCookie.httpOnly(), httpCookie.httpOnly(), cookie.isHttpOnly());
		assertEquals("MaxAge must be equal to " + httpCookie.maxAge(), httpCookie.maxAge().intValue(), cookie.getMaxAge());
		assertEquals("Path must be equal to " + httpCookie.path(), httpCookie.path(), cookie.getPath());
		assertEquals("Secure must be equal to " + httpCookie.secure(), httpCookie.secure(), cookie.isSecure());
	}

	@Test
	public final void testGetServerPort() {
		// given
		String host = "localhost";
		String port = "8080";
		doReturn(host + ":" + port).when(requestMock).host();
		
		// when
		int result = playWebContext.getServerPort();

		// then
		assertEquals("Result value must be equal to port.", port, String.valueOf(result));
	}
	
	@Test
	public final void testGetServerPortDefaultPort() {
		// given
		String host = "localhost";
		String defaultPort = "80";
		doReturn(host).when(requestMock).host();
		
		// when
		int result = playWebContext.getServerPort();
		
		// then
		assertEquals("Result value must be equal to " + defaultPort, defaultPort, String.valueOf(result));
	}

	@Test
	public final void testGetSchemeSecure() {
		// given
		doReturn(true).when(requestMock).secure();
		
		// when
		String result = playWebContext.getScheme();
		
		// then
		assertEquals("Result value must be equal to https", "https", result);
	}

	@Test
	public final void testGetSchemeNotSecure() {
		// given
		doReturn(false).when(requestMock).secure();
		
		// when
		String result = playWebContext.getScheme();
		
		// then
		assertEquals("Result value must be equal to http", "http", result);
	}
}
