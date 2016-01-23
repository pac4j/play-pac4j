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
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import play.cache.Cache;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Http.Session;

/**
 * 
 * @author furkan yavuz
 * @since 2.1.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Http.Context.class, Cache.class})
public class UserProfileControllerTest {
	
	@SuppressWarnings("rawtypes")
	@Test
	public final void testGetUserProfile() {
		// given
		String sessionId = "SESSION_ID";
		Session sessionMock = mock(Session.class);
		Context contextMock = mock(Context.class);
		String key = "$" + sessionId + "$" + Pac4jConstants.USER_PROFILE;
		
		doReturn(sessionMock).when(contextMock).session();
		doReturn(sessionId).when(sessionMock).get(Pac4jConstants.SESSION_ID);;
		
		contextMock.args = new HashMap<>();
		mockStatic(Http.Context.class);
		when(Http.Context.current()).thenReturn(contextMock);
		
		UserProfile userProfileMock = mock(CommonProfile.class);
		mockStatic(Cache.class);
		when(Cache.get(key)).thenReturn(userProfileMock);
		
		UserProfileController userProfileController = new UserProfileController<>();
		Config configMock = mock(Config.class);
		setInternalState(userProfileController, "config", configMock);
		
		// when
		UserProfile userProfile = userProfileController.getUserProfile();
		
		// then
		assertEquals("UserProfile must be equal to userProfileMock.", userProfileMock, userProfile);
	}

}
