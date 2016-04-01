package org.pac4j.play.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.play.PlayWebContext;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import play.libs.F.Promise;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;

/**
 * @author furkan yavuz
 * @since 2.1.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Proxy.class, SecurityAction.class, Promise.class, IndirectClient.class })
public class SecureActionCallTest {

	private SecurityAction securityAction;
	private Config configMock;
	private Context contextMock;
	private Clients clientsMock;
	private Request requestMock;
	private InvocationHandler invocationHandlerMock;
	private HttpActionAdapter httpActionAdapterMock;

	@Before
	public void setUp() throws Exception {
		configMock = mock(Config.class);
		contextMock = mock(Context.class);
		clientsMock = mock(Clients.class);
		requestMock = mock(Request.class);
		invocationHandlerMock = mock(InvocationHandler.class);
		httpActionAdapterMock = mock(HttpActionAdapter.class);

		doReturn(clientsMock).when(configMock).getClients();
		doReturn(requestMock).when(contextMock).request();
		doReturn(httpActionAdapterMock).when(configMock).getHttpActionAdapter();

		securityAction = spy(new SecurityAction(configMock));
	}

	@SuppressWarnings("unchecked")
	@Test
	public final void testCall() throws Throwable {
		// given
		Promise<Result> resultMock = mock(Promise.class);
		doReturn(resultMock).when(securityAction).internalCall(contextMock, null, null);

		mockStatic(Proxy.class);
		doReturn(invocationHandlerMock).when(Proxy.class);
		Proxy.getInvocationHandler(null);

		// when
		Promise<Result> result = securityAction.call(contextMock);

		// then
		assertEquals("Result must be equal to resultMock.", resultMock, result);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public final void testInternalCall() throws Throwable {
		mockStatic(Proxy.class);
		doReturn(invocationHandlerMock).when(Proxy.class);
		Proxy.getInvocationHandler(null);
		
		ProfileManager profileManagerMock = mock(ProfileManager.class);
		whenNew(ProfileManager.class).withAnyArguments().thenReturn(profileManagerMock);

		// when
		Promise<Result> result = securityAction.internalCall(contextMock, null, null);

		// then
		assertNotNull("Result must be set.", result);
	}

	@Test
	public final void testForbidden() {
		// given
		mockStatic(Promise.class);
		PlayWebContext playWebContextMock = mock(PlayWebContext.class);

		// when
		securityAction.forbidden(playWebContextMock, null, null);

		// then
		verifyStatic(atLeastOnce());
        Promise.pure((Result) configMock.getHttpActionAdapter().adapt(HttpConstants.FORBIDDEN, playWebContextMock));
	}

	@Test
	public final void testUnauthorized() {
		// given
		mockStatic(Promise.class);
		PlayWebContext playWebContextMock = mock(PlayWebContext.class);

		// when
		securityAction.unauthorized(playWebContextMock, null);

		// then
		verifyStatic(atLeastOnce());
		Promise.pure((Result) configMock.getHttpActionAdapter().adapt(HttpConstants.UNAUTHORIZED, playWebContextMock));
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public final void testRedirectToIdentityProvider() throws RequiresHttpAction {
		// given
		Result resultMock = mock(Result.class);
		HttpActionAdapter httpActionAdapterMock = mock(HttpActionAdapter.class);
		PlayWebContext contextMock = mock(PlayWebContext.class);
		IndirectClient clientMock = mock(IndirectClient.class);
		
		doReturn(httpActionAdapterMock).when(configMock).getHttpActionAdapter();
		doReturn(resultMock).when(httpActionAdapterMock).adapt(0, contextMock);
		
		List<Client> currentClients = new ArrayList<>();
		currentClients.add(clientMock);

		// when
		Result result = securityAction.redirectToIdentityProvider(contextMock, currentClients);

		// then
		assertEquals("Result must be equal to resultMock", resultMock, result);
	}
}
