package org.pac4j.play.store;

import org.junit.Test;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.play.PlayWebContext;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public final class PlayCookieSessionStoreTests {

    @Test
    public void testInjectedStoreSurvivesApplicationRestart() {
        final Map<String, String> session = writeWithApplication("persistent-application-secret-for-session-tests");
        final Application restarted = application("persistent-application-secret-for-session-tests");
        try {
            final PlayCookieSessionStore store = restarted.injector().instanceOf(PlayCookieSessionStore.class);
            final PlayWebContext context = context(session);
            assertEquals(Optional.of("value"), store.get(context, "key"));
            assertEquals("dark", context.getNativeSession().get("theme").orElseThrow());
        } finally {
            play.test.Helpers.stop(restarted);
        }
    }

    @Test
    public void testSecretRotationDiscardsOldCookieAndAllowsNewSession() {
        final Map<String, String> session = writeWithApplication("old-application-secret-for-session-tests");
        final Application rotated = application("new-application-secret-for-session-tests");
        try {
            final PlayCookieSessionStore store = rotated.injector().instanceOf(PlayCookieSessionStore.class);
            final PlayWebContext context = context(session);
            assertFalse(store.get(context, "key").isPresent());
            assertFalse(context.getNativeSession().get("pac4j").isPresent());
            assertEquals(Optional.of("dark"), context.getNativeSession().get("theme"));
            store.set(context, "key", "new-value");
            assertEquals(Optional.of("new-value"), store.get(context(context.getNativeSession().data()), "key"));
        } finally {
            play.test.Helpers.stop(rotated);
        }
    }

    @Test
    public void testMalformedCookiesAreDiscarded() {
        final PlayCookieSessionStore store = new PlayCookieSessionStore(new JdkAesDataEncrypter(new byte[16]));
        for (final String cookie : new String[]{"*not-base64*", Base64.getEncoder().encodeToString(new byte[]{1, 2, 3})}) {
            final PlayWebContext context = context(Map.of("pac4j", cookie, "theme", "dark"));
            assertFalse(store.get(context, "key").isPresent());
            assertEquals(Map.of("theme", "dark"), context.getNativeSession().data());
            store.set(context, "key", "value");
            assertEquals(Optional.of("value"), store.get(context(context.getNativeSession().data()), "key"));
        }
    }

    @Test
    public void testManualStoreRequiresExplicitEncrypter() {
        final PlayCookieSessionStore store = new PlayCookieSessionStore();
        final TechnicalException error = assertThrows(TechnicalException.class,
                () -> store.set(context(Map.of()), "key", "value"));
        assertTrue(error.getMessage().contains("persistent shared key"));
    }

    @Test
    public void testManuallyConfiguredSharedKeyWorksAcrossInstances() {
        final PlayCookieSessionStore writer = new PlayCookieSessionStore();
        writer.setDataEncrypter(new JdkAesDataEncrypter(new byte[16]));
        final PlayWebContext context = context(Map.of());
        writer.set(context, "key", "value");
        final PlayCookieSessionStore reader = new PlayCookieSessionStore(new JdkAesDataEncrypter(new byte[16]));
        assertEquals(Optional.of("value"), reader.get(context(context.getNativeSession().data()), "key"));
    }

    private Map<String, String> writeWithApplication(final String secret) {
        final Application application = application(secret);
        try {
            final PlayCookieSessionStore store = application.injector().instanceOf(PlayCookieSessionStore.class);
            final PlayWebContext context = context(Map.of("theme", "dark"));
            store.set(context, "key", "value");
            return context.getNativeSession().data();
        } finally {
            play.test.Helpers.stop(application);
        }
    }

    private Application application(final String secret) {
        return new GuiceApplicationBuilder().configure("play.http.secret.key", secret).build();
    }

    private PlayWebContext context(final Map<String, String> session) {
        return new PlayWebContext(new Http.RequestBuilder().session(session).build());
    }
}
