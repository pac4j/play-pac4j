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
package org.pac4j.play.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.play.PlayWebContext;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

import play.cache.Cache;
import play.mvc.Http.Session;

/**
 * 
 * @author furkan yavuz
 * @since 2.1.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Cache.class })
public class PlayCacheStoreTest {
	
	private PlayCacheStore playCacheStore;
	private PlayWebContext playWebContext;
	private Session session;
	private final String SESSION_ID = "bf7b5d0d-f4db-464f-a79e-53f61186adec";
	private final String CACHE_KEY = "$" + SESSION_ID + "$" + Pac4jConstants.SESSION_ID;


	@Before
	public void setUp() throws Exception {
		playCacheStore = new PlayCacheStore();
		
		session = mock(Session.class);
		playWebContext = mock(PlayWebContext.class);
		
		doReturn(session).when(playWebContext).getJavaSession();
	}

	@Test
	public final void testGetKey() {
		// when
		String result = playCacheStore.getKey(SESSION_ID, Pac4jConstants.SESSION_ID);
		
		// then
		assertEquals("Result must be equal to " + CACHE_KEY, CACHE_KEY, result);
	}

	@Test
	public final void testGetOrCreateSessionId() {
		// when
		String result = playCacheStore.getOrCreateSessionId(playWebContext);
		
		// then
		assertNotNull("Result must be filled with random value.", result);
	}

	@Test
	public final void testGet() {
		// given
		doReturn(SESSION_ID).when(session).get(Pac4jConstants.SESSION_ID);
		mockStatic(Cache.class);
		
		// when
		playCacheStore.get(playWebContext, Pac4jConstants.SESSION_ID);
		
		// then
		verifyStatic();
		Cache.get(CACHE_KEY);
	}

	@Test
	public final void testSet() {
		// given
		Object value = new Object();
		doReturn(SESSION_ID).when(session).get(Pac4jConstants.SESSION_ID);
		mockStatic(Cache.class);
		
		// when
		playCacheStore.set(playWebContext, Pac4jConstants.SESSION_ID, value);
		
		// then
		verifyStatic();
		Cache.set(CACHE_KEY, value, 60);
	}

	@Test
	public final void testSetUserProfile() {
		// given
		Object value = new UserProfile();
		doReturn(SESSION_ID).when(session).get(Pac4jConstants.SESSION_ID);
		mockStatic(Cache.class);
		
		// when
		playCacheStore.set(playWebContext, Pac4jConstants.SESSION_ID, value);
		
		// then
		verifyStatic();
		Cache.set(CACHE_KEY, value, 3600);
	}

	@Test
	public final void testGetterSetter() {
		Validator validator = ValidatorBuilder.create().with(new SetterTester()).with(new GetterTester()).build();
		validator.validate(PojoClassFactory.getPojoClass(PlayCacheStore.class));
	}
}
