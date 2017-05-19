package org.pac4j.play.store;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.util.TestsConstants;
import org.pac4j.play.PlayWebContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import play.cache.CacheApi;
import play.mvc.Http;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Tests {@link PlayCacheSessionStore}.
 *
 * @author Jerome Leleu
 * @since 2.3.0
 */
@RunWith(PowerMockRunner.class)
public final class PlayCacheSessionStoreTests implements TestsConstants {

    private PlayCacheSessionStore store;
    private PlayWebContext context;
    private CacheApi cacheApiMock;

    @Before
    public void setUp() {
        cacheApiMock = mock(CacheApi.class);
        store = new PlayCacheSessionStore(cacheApiMock);
        final Http.Session session = mock(Http.Session.class);
        context = mock(PlayWebContext.class);
        when(context.getJavaSession()).thenReturn(session);
    }

    @Test
    public void testKey() {
        assertEquals("$id$key", store.getKey(ID, KEY));
    }

    @Test
    public void testGetOrCreateSessionId() {
        assertNotNull(store.getOrCreateSessionId(context));
    }

    @Test
    @PrepareForTest(CacheApi.class)
    public void testGetSet() {
        mockStatic(CacheApi.class);
        PowerMockito.when(cacheApiMock.get(any(String.class))).thenReturn(VALUE);
        store.setPrefix(KEY);
        store.set(context, KEY, VALUE);
        assertEquals(VALUE, store.get(context, KEY));
    }
}
