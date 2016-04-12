package org.pac4j.play.cas.logout;

import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.store.PlayCacheStore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import play.cache.Cache;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Http.Session;

/**
 * @author furkan yavuz
 * @since 2.1.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class, Request.class, PlayWebContext.class, SessionStore.class, PlayCacheStore.class, Cache.class, PlayCacheLogoutHandler.class })
public class PlayCacheLogoutHandlerTest {
	
	private PlayCacheLogoutHandler playCacheLogoutHandler;
	private PlayCacheStore playCacheStoreMock;
	private PlayWebContext contextMock;

	@Before
	public void setUp() throws Exception {
		playCacheLogoutHandler = new PlayCacheLogoutHandler();
		playCacheStoreMock = mock(PlayCacheStore.class);
		contextMock = mock(PlayWebContext.class);
	
		doReturn(playCacheStoreMock).when(contextMock).getSessionStore();

		mockStatic(Cache.class);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public final void testDestroySession() throws Exception {
		// given
		Session sessionMock = mock(Session.class);
		doReturn(sessionMock).when(contextMock).getJavaSession();
		ProfileManager profileManagerMock = mock(ProfileManager.class);
		whenNew(ProfileManager.class).withArguments(contextMock).thenReturn(profileManagerMock);
		
		// when
		playCacheLogoutHandler.destroySession(contextMock);
		
		// then
		verify(profileManagerMock).remove(true);
	}

	@Test
	public final void testRecordSession() {
		// when
		playCacheLogoutHandler.recordSession(contextMock, null);
		
		// then
		verifyStatic();
		Cache.set(null, null, 0);
	}
	
	@SuppressWarnings({ "deprecation", "unused" })
	@Test
	public final void testConstructor() {
		// when
		PlayCacheStore playCacheStoreMock = mock(PlayCacheStore.class);
		PlayCacheLogoutHandler playCacheLogoutHandler = null; // new PlayCacheLogoutHandler(playCacheStoreMock);
		
		// then
		verifyZeroInteractions(playCacheStoreMock);
	}
}
