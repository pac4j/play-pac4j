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
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.play.store.PlayCacheStore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Http.Session;

/**
 *
 * @author furkan yavuz
 * @since 2.1.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ApplicationLogoutController.class, PlayWebContext.class, Http.Context.class})
public class ApplicationLogoutControllerTest {

	private ApplicationLogoutController applicationLogoutController;
	private Config config;
	private Context contextMock;
	private Request requestMock;
	private Session sessionMock;
	private SessionStore sessionStoreMock;

	@Before
	public void setUp() throws Exception {
		applicationLogoutController = new ApplicationLogoutController();
  
		config = mock(Config.class);
		contextMock = mock(Context.class);
		requestMock = mock(Request.class);
		sessionMock = mock(Session.class);
		sessionStoreMock = mock(SessionStore.class);

		doReturn(sessionMock).when(contextMock).session();
		doReturn(requestMock).when(contextMock).request();
		doReturn(sessionStoreMock).when(config).getSessionStore();
	}

	@Test
	public final void testLogout() {
		// given
		mockStatic(Http.Context.class);
		when(Http.Context.current()).thenReturn(contextMock);
		setInternalState(applicationLogoutController, "config", config);

		contextMock.args = new HashMap<String, Object>();
		String key = "KEY";
		Object value = mock(Object.class);
		contextMock.args.put(key, value);

		// when
		Result result = applicationLogoutController.logout();

		// then
		assertEquals("Status must be 200", 200, result.status());
	}

	@Test
	public final void testLogoutDefaultUrlRedirect() {
		// given
		mockStatic(Http.Context.class);
		when(Http.Context.current()).thenReturn(contextMock);
		setInternalState(applicationLogoutController, "config", config);
		Map<String, String[]> urlParameters = new HashMap<>();
		urlParameters.put("url", new String[]{"url"});
		doReturn(urlParameters).when(requestMock).queryString();

		contextMock.args = new HashMap<String, Object>();
		String key = "KEY";
		Object value = mock(Object.class);
		contextMock.args.put(key, value);

		// when
		Result result = applicationLogoutController.logout();

		// then
		assertEquals("Request have url parameter therefore logout must return result with 303 code", 303, result.status());
		assertEquals("Location must be " + Pac4jConstants.DEFAULT_URL_VALUE, Pac4jConstants.DEFAULT_URL_VALUE, result.header("LOCATION").get());
	}

	@Test
	public final void testLogoutDefaultLogoutUrlRedirect() {
		// given
		mockStatic(Http.Context.class);
		when(Http.Context.current()).thenReturn(contextMock);
		setInternalState(applicationLogoutController, "config", config);
		Map<String, String[]> urlParameters = new HashMap<>();
		urlParameters.put("url", new String[]{Pac4jConstants.DEFAULT_LOGOUT_URL_PATTERN_VALUE});
		doReturn(urlParameters).when(requestMock).queryString();

		contextMock.args = new HashMap<String, Object>();
		String key = "KEY";
		Object value = mock(Object.class);
		contextMock.args.put(key, value);

		// when
		Result result = applicationLogoutController.logout();

		// then
		assertEquals("Request have url parameter therefore logout must return result with 303 code", 303, result.status());
		assertEquals("Location must be " + Pac4jConstants.DEFAULT_LOGOUT_URL_PATTERN_VALUE, Pac4jConstants.DEFAULT_LOGOUT_URL_PATTERN_VALUE, result.header("LOCATION").get());
	}

	@Test
	public final void testGetterSetter() {
		Validator validator = ValidatorBuilder.create().with(new SetterTester()).with(new GetterTester()).build();
		validator.validate(PojoClassFactory.getPojoClass(ApplicationLogoutController.class));
	}

}
