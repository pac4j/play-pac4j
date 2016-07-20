package org.pac4j.play.cas.logout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.util.TestsConstants;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.store.PlayCacheStore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import play.cache.CacheApi;
import play.mvc.Http;

import java.util.LinkedHashMap;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

/**
 * Tests {@link PlayCacheLogoutHandler}.
 *
 * @author Jerome Leleu
 * @since 2.3.0
 */
@RunWith(PowerMockRunner.class)
public final class PlayCacheLogoutHandlerTests implements TestsConstants {

    private PlayCacheLogoutHandler handler;

    private PlayWebContext context;
    private CacheApi cacheApiMock;

    @Before
    public void setUp() {
        cacheApiMock = mock(CacheApi.class);
        handler = new PlayCacheLogoutHandler(cacheApiMock);
        context = mock(PlayWebContext.class);
    }

    @Test
    @PrepareForTest(CacheApi.class)
    public void testRecord() {
        mockStatic(CacheApi.class);
        when(context.getSessionStore()).thenReturn(new PlayCacheStore(cacheApiMock));
        final Http.Session session = mock(Http.Session.class);
        when(context.getJavaSession()).thenReturn(session);
        handler.recordSession(context, KEY);
        verifyStatic();

    }

    @Test
    @PrepareForTest(CacheApi.class)
    public void testDestroy() {
        mockStatic(CacheApi.class);
        when(context.getRequestParameter("logoutRequest")).thenReturn("SessionIndex>" + VALUE + "</");
        final Http.Session session = mock(Http.Session.class);
        when(context.getJavaSession()).thenReturn(session);
        when(cacheApiMock.get(VALUE)).thenReturn(KEY);
        handler.destroySession(context);
        verify(session).put(Pac4jConstants.SESSION_ID, KEY);
        verify(context).setRequestAttribute(Pac4jConstants.USER_PROFILES, new LinkedHashMap());
        verifyStatic();
    }
}
