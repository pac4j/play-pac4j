package org.pac4j.play.store;

import org.junit.Test;
import org.pac4j.core.util.TestsConstants;
import org.pac4j.play.PlayWebContext;
import play.mvc.Http;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link PlayCookieSessionStore}.
 *
 * @author Jerome Leleu
 * @since 6.1.0
 */
public final class PlayCookieSessionStoreTests implements TestsConstants {

    private PlayCookieSessionStore store = new PlayCookieSessionStore(new ShiroAesDataEncrypter());

    @Test
    public void testOk() {

        final Map<String, String> session = new HashMap<>();

        final PlayWebContext context = mock(PlayWebContext.class);
        when(context.getJavaSession()).thenReturn(new Http.Session(session));

        store.set(context, KEY, PAC4J_URL);

        final Optional<Object> value = store.get(context, KEY);
        assertEquals(Optional.of(PAC4J_URL), value);
    }
}
