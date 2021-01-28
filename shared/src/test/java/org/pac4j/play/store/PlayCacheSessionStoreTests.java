package org.pac4j.play.store;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.core.util.TestsConstants;
import org.pac4j.play.PlayWebContext;
import play.cache.SyncCacheApi;
import play.mvc.Http;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link PlayCacheSessionStore}.
 *
 * @author Jerome Leleu
 * @since 2.3.0
 */
public final class PlayCacheSessionStoreTests implements TestsConstants {

    private PlayCacheSessionStore store;
    private PlayWebContext context;
    private SyncCacheApi cacheApiMock;

    @Before
    public void setUp() {
        cacheApiMock = mock(SyncCacheApi.class);
        store = new PlayCacheSessionStore(cacheApiMock);
        context = mock(PlayWebContext.class);
        when(context.getNativeSession()).thenReturn(mock(Http.Session.class));
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
    public void testGetSet() {
        final Map<String, Object> data = new HashMap<>();
        data.put(KEY, VALUE);
        when(cacheApiMock.getOptional(any(String.class))).thenReturn(Optional.of(data));
        store.setPrefix(KEY);
        store.set(context, KEY, VALUE);
        Optional<Object> value = store.get(context, KEY);
        assertEquals(Optional.of(VALUE), value);
    }

    @Test
    public void testDestroySession() {
        when(context.getRequestAttribute(Pac4jConstants.SESSION_ID)).thenReturn(Optional.of(KEY));

        store.destroySession(context);

        verify(cacheApiMock, times(1)).remove(KEY);
    }
}
