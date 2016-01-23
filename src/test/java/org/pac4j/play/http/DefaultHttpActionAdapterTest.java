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
package org.pac4j.play.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.doReturn;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.http.DefaultHttpActionAdapter;

import play.mvc.Result;

/**
 * 
 * @author furkan yavuz
 * @since 2.1.0
 */
public class DefaultHttpActionAdapterTest {
	
	private DefaultHttpActionAdapter defaultHttpActionAdapter;
	
	@Before
	public void setUp() throws Exception {
		defaultHttpActionAdapter = new DefaultHttpActionAdapter();
	}

	@Test
	public final void testAdaptUnauthorized() {
		// when
		Object result = defaultHttpActionAdapter.adapt(HttpConstants.UNAUTHORIZED, null);
		
		// then
		assertEquals("Status must be equal to " + HttpConstants.UNAUTHORIZED, HttpConstants.UNAUTHORIZED, ((Result) result).status());
	}

	@Test
	public final void testAdaptForbidden() {
		// when
		Object result = defaultHttpActionAdapter.adapt(HttpConstants.FORBIDDEN, null);
		
		// then
		assertEquals("Status must be equal to " + HttpConstants.FORBIDDEN, HttpConstants.FORBIDDEN, ((Result) result).status());
	}
	
	@Test
	public final void testAdaptRedirect() {
		// given
		PlayWebContext playWebContext = mock(PlayWebContext.class);
		
		// when
		Object result = defaultHttpActionAdapter.adapt(HttpConstants.TEMP_REDIRECT, playWebContext);
		
		// then
		assertEquals("Status must be equal to " + 303, 303, ((Result) result).status());
	}
	
	@Test
	public final void testAdaptOk() {
		// given
		PlayWebContext playWebContext = mock(PlayWebContext.class);
		doReturn("TEST").when(playWebContext).getResponseContent();
		
		// when
		Object result = defaultHttpActionAdapter.adapt(HttpConstants.OK, playWebContext);
		
		// then
		assertEquals("Status must be equal to " + HttpConstants.OK, HttpConstants.OK, ((Result) result).status());
	}
	
	@Test(expected = TechnicalException.class)
	public final void testAdaptTechnicalException() {
		// when
		defaultHttpActionAdapter.adapt(0, null);
		
		// then
		fail("TechnicalException expected.");
	}
}
