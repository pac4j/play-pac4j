package org.pac4j.play.store;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.core.util.TestsConstants;
import org.pac4j.play.PlayWebContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import play.cache.SyncCacheApi;
import play.mvc.Http;

import java.util.HashMap;
import java.util.Map;

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
    private SyncCacheApi cacheApiMock;

    @Before
    public void setUp() {
        cacheApiMock = mock(SyncCacheApi.class);
        store = new PlayCacheSessionStore(cacheApiMock);
        final Http.Session session = mock(Http.Session.class);
        context = mock(PlayWebContext.class);
        when(context.getJavaSession()).thenReturn(session);
    }

    @Test
    public void testNoPrefixSessionKey() {
        assertEquals(ID, store.getPrefixedSessionKey(ID));
    }

    @Test
    public void testPrefixedSessionKey() {
        store.setPrefix(VALUE);
        assertEquals(VALUE + ID, store.getPrefixedSessionKey(ID));
    }

    @Test
    public void testGetOrCreateSessionId() {
        assertNotNull(store.getOrCreateSessionId(context));
    }

    @Test
    @PrepareForTest(SyncCacheApi.class)
    public void testGetSet() {
        mockStatic(SyncCacheApi.class);
        final Map<String, Object> data = new HashMap<>();
        data.put(KEY, VALUE);
        PowerMockito.when(cacheApiMock.get(any(String.class))).thenReturn(data);
        store.setPrefix(KEY);
        store.set(context, KEY, VALUE);
        assertEquals(VALUE, store.get(context, KEY));
    }
}
