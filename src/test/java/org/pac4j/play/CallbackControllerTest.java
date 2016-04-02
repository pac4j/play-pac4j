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
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.reflect.Whitebox.setInternalState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.play.store.PlayCacheStore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.LoggerFactory;

import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Http.Session;
import play.mvc.Result;

/**
 * Unit test cases for CallbackController.java class
 *
 * @author furkan yavuz
 * @since 2.1.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ CallbackController.class, PlayWebContext.class, Http.Context.class})
public class CallbackControllerTest {

	private CallbackController callbackController;
	private Config config;
	private Context contextMock;
	private Request requestMock;
	private Session sessionMock;
	private Response responseMock;
	private SessionStore sessionStoreMock;

	private String clientName = "TEST_CLIENT";

	@Before
	public void setUp() throws Exception {
		callbackController = new CallbackController();

		config = mock(Config.class);
		contextMock = mock(Context.class);
		requestMock = mock(Request.class);
		sessionMock = mock(Session.class);
		responseMock = mock(Response.class);
		sessionStoreMock = mock(SessionStore.class);

		doReturn(sessionMock).when(contextMock).session();
		doReturn(requestMock).when(contextMock).request();
		doReturn(responseMock).when(contextMock).response();
		doReturn(sessionStoreMock).when(config).getSessionStore();
	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Test
	public final void testCallback() throws Exception {
		// given
		Map<String, String[]> urlParameters = new HashMap<>();
		urlParameters.put("client_name", new String[]{clientName});

		HttpActionAdapter httpActionAdapter = mock(HttpActionAdapter.class);
		IndirectClient clientMock = mock(IndirectClient.class);
		setInternalState(clientMock, "logger", LoggerFactory.getLogger(getClass()));

		Clients clients = new Clients();
		clients.setClientsList(Arrays.asList(clientMock));

		setInternalState(clients, "initialized", true);
		setInternalState(callbackController, "config", config);

		doReturn(clientName).when(clientMock).getName();
		doReturn(httpActionAdapter).when(config).getHttpActionAdapter();
		doReturn(clients).when(config).getClients();
		doReturn(urlParameters).when(requestMock).queryString();

		mockStatic(Http.Context.class);
		when(Http.Context.current()).thenReturn(contextMock);
		String expected = "/";

		// when
		Result result = callbackController.callback();

		// then
		assertEquals("Location must be equal to /", expected, result.header("Location").get());
	}

	@SuppressWarnings("unchecked")
	@Test
	public final void testSaveUserProfile() throws Exception {
		// given
		UserProfile profile = mock(UserProfile.class);
		ProfileManager<UserProfile> profileManager = mock(ProfileManager.class);
		whenNew(ProfileManager.class).withAnyArguments().thenReturn(profileManager);

		// when
		callbackController.saveUserProfile(null, profile);

		// then
		verify(profileManager).save(true, profile);
	}

	@Test
	public final void testRedirectToOriginallyRequestedUrlDefaultUrl() {
		// given
		WebContext context = mock(WebContext.class);
		String expected = "/";

		// when
		Result result = callbackController.redirectToOriginallyRequestedUrl(context);

		// then
		assertEquals("Location must be equal to /", expected, result.header("Location").get());
	}

	@Test
	public final void testRedirectToOriginallyRequestedUrl() {
		// given
		WebContext context = mock(WebContext.class);
		String expected = "/test";
		doReturn(expected).when(context).getSessionAttribute(Pac4jConstants.REQUESTED_URL);

		// when
		Result result = callbackController.redirectToOriginallyRequestedUrl(context);

		// then
		assertEquals("Location must be equal to " + expected, expected, result.header("Location").get());
	}

	@Test
	public final void testGetterSetter() {
		Validator validator = ValidatorBuilder.create().with(new SetterTester()).with(new GetterTester()).build();
		validator.validate(PojoClassFactory.getPojoClass(CallbackController.class));
	}
}
