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
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.reflect.Whitebox.getInternalState;

import org.junit.Test;
import org.pac4j.core.config.Config;

/**
 * 
 * @author furkan yavuz
 * @since 2.1.0
 */
public class RequiresAuthenticationActionConstructorTest {
	
	private RequiresAuthenticationAction requiresAuthenticationAction;
	private Config configMock;

	@Test
	public final void testConstructorConfig() {
		// given
		configMock = mock(Config.class);
		
		// when
		requiresAuthenticationAction = new RequiresAuthenticationAction(configMock);
		
		// then
		Config config = getInternalState(requiresAuthenticationAction, "config");
		assertEquals("Config field must be set.", configMock, config);
	}
	
}
