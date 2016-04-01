package org.pac4j.play.java;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author furkan yavuz
 * @since 2.1.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ SecurityAction.class })
public class SecureActionTest {

	private SecurityAction securityAction;

	@Before
	public void setUp() throws Exception {
		securityAction = new SecurityAction();
	}

	@Test
	public final void testUseSessionCurrentClientsNull() {
		// when
		boolean result = securityAction.useSession(null, null);

		// then
		assertTrue("CurrentClients is null therefore result must be true", result);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public final void testUseSessionCurrentClientsSet() {
		// given
		List<Client> currentClients = new ArrayList<>();
		currentClients.add(mock(Client.class));
		
		// when
		boolean result = securityAction.useSession(null, currentClients);

		// then
		assertFalse("CurrentClients have one non-inderectClient therefore result must be false", result);
	}

	@Test
	public final void testStartAuthenticationCurrentClientsNull() {
		// when
		boolean result = securityAction.startAuthentication(null, null);

		// then
		assertFalse("CurrentClients is null therefore result must be false", result);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public final void testStartAuthenticationCurrentClientsSet() {
		// given
		List<Client> currentClients = new ArrayList<>();
		currentClients.add(mock(IndirectClient.class));

		// when
		boolean result = securityAction.startAuthentication(null, currentClients);

		// then
		assertTrue("CurrentClients have one InderectClient therefore result must be true", result);
	}

	@Test
	public final void testSaveRequestedUrl() {
		// given
		String requestedUrl = "URL";
		WebContext webContextMock = mock(WebContext.class);
		doReturn(requestedUrl).when(webContextMock).getFullRequestURL();

		// when
		securityAction.saveRequestedUrl(webContextMock, null);

		// then
		verify(webContextMock).setSessionAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
	}
}
