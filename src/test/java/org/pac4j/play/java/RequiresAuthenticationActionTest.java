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
 * 
 * @author furkan yavuz
 * @since 2.1.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ RequiresAuthenticationAction.class })
public class RequiresAuthenticationActionTest {

	private RequiresAuthenticationAction requiresAuthenticationAction;

	@Before
	public void setUp() throws Exception {
		requiresAuthenticationAction = new RequiresAuthenticationAction();
	}

	@Test
	public final void testUseSessionCurrentClientsNull() {
		// when
		boolean result = requiresAuthenticationAction.useSession(null, null);

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
		boolean result = requiresAuthenticationAction.useSession(null, currentClients);

		// then
		assertFalse("CurrentClients have one non-inderectClient therefore result must be false", result);
	}

	@Test
	public final void testStartAuthenticationCurrentClientsNull() {
		// when
		boolean result = requiresAuthenticationAction.startAuthentication(null, null);

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
		boolean result = requiresAuthenticationAction.startAuthentication(null, currentClients);

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
		requiresAuthenticationAction.saveRequestedUrl(webContextMock, null);

		// then
		verify(webContextMock).setSessionAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
	}

}
