package org.pac4j.play;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.pac4j.test.util.TestsConstants;

import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Results;
import org.pac4j.core.context.Cookie;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link PlayWebContext}.
 *
 * @author Ivan Suhinin
 * @since 3.0.0
 */
public final class PlayWebContextTests implements TestsConstants {

    private Request requestMock;
    private PlayWebContext webContext;

    private static final String domainWithoutPort = "somedomain.com";
    
    @Before
    public void setUp() {
        requestMock = mock(Request.class);
        webContext = new PlayWebContext(requestMock);
    }
    
    @After
    public void teardown() {
    }

    @Test
    public void testRandomNonSecureServerPort() {
        final Integer port = 9000;
        String host = domainWithoutPort + ":" + port.toString();

        when(requestMock.secure()).thenReturn(false);
        when(requestMock.host()).thenReturn(host);

        assertEquals(port.intValue(), webContext.getServerPort());
    }

    @Test
    public void testRandomSecureServerPort() {
        final Integer port = 9000;
        String host = domainWithoutPort + ":" + port.toString();

        when(requestMock.secure()).thenReturn(true);
        when(requestMock.host()).thenReturn(host);

        assertEquals(port.intValue(), webContext.getServerPort());
    }

    @Test
    public void testNonSecureServerPort() {
        when(requestMock.secure()).thenReturn(false);
        when(requestMock.host()).thenReturn(domainWithoutPort);

        assertEquals(80, webContext.getServerPort());
    }

    @Test
    public void testSecureServerPort() {
        when(requestMock.secure()).thenReturn(true);
        when(requestMock.host()).thenReturn(domainWithoutPort);

        assertEquals(443, webContext.getServerPort());
    }

    @Test
    public void testMergeSessionUpdatesAndRemovals() {
        final Request request = new Http.RequestBuilder().session(Map.of("sid", "old", "removed", "value", "theme", "light")).build();
        final PlayWebContext context = new PlayWebContext(request);
        context.setNativeSession(request.session().adding("sid", "new").removing("removed"));
        final Http.Session resultSession = context.supplementResponse(Results.ok().withSession(request.session().adding("controller", "value"))).session();
        assertEquals(Optional.of("new"), resultSession.get("sid"));
        assertFalse(resultSession.get("removed").isPresent());
        assertEquals(Optional.of("light"), resultSession.get("theme"));
        assertEquals(Optional.of("value"), resultSession.get("controller"));
    }

    @Test
    public void testPreserveControllerSessionChanges() {
        final Request request = new Http.RequestBuilder().session(Map.of("changed", "initial", "removed", "initial")).build();
        final PlayWebContext context = new PlayWebContext(request);
        context.setNativeSession(request.session().adding("changed", "pac4j").adding("created", "pac4j").removing("removed"));
        final Http.Session controllerSession = request.session().adding("changed", "controller").adding("removed", "controller");
        final Http.Session resultSession = context.supplementResponse(Results.ok().withSession(controllerSession)).session();
        assertEquals(Optional.of("controller"), resultSession.get("changed"));
        assertEquals(Optional.of("controller"), resultSession.get("removed"));
        assertEquals(Optional.of("pac4j"), resultSession.get("created"));
    }

    @Test
    public void testPreserveControllerSessionRemoval() {
        final Request request = new Http.RequestBuilder().session("sid", "old").build();
        final PlayWebContext context = new PlayWebContext(request);
        context.setNativeSession(request.session().adding("sid", "new"));
        assertFalse(context.supplementResponse(Results.ok().withSession(Map.of())).session().get("sid").isPresent());
    }

    @Test
    public void testNoSessionCookieWhenSessionIsUnchanged() {
        final PlayWebContext context = new PlayWebContext(new Http.RequestBuilder().session("sid", "unchanged").build());
        assertFalse(context.hasResponseModifications());
        assertNull(context.supplementResponse(Results.ok()).session());
    }

    @Test
    public void testDeleteCookieWithUnchangedValue() {
        final PlayWebContext context = new PlayWebContext(new Http.RequestBuilder().cookie(Http.Cookie.builder("token", "same").build()).build());
        final Cookie cookie = new Cookie("token", "same");
        cookie.setMaxAge(0);
        context.addResponseCookie(cookie);
        assertEquals(Integer.valueOf(0), context.supplementResponse(Results.ok()).cookie("token").orElseThrow().maxAge());
    }

    @Test
    public void testRefreshCookieWithUnchangedValue() {
        final PlayWebContext context = new PlayWebContext(new Http.RequestBuilder().cookie(Http.Cookie.builder("token", "same").build()).build());
        final Cookie cookie = new Cookie("token", "same");
        cookie.setMaxAge(3600);
        cookie.setPath("/secure");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        context.addResponseCookie(cookie);
        final Http.Cookie responseCookie = context.supplementResponse(Results.ok()).cookie("token").orElseThrow();
        assertEquals(Integer.valueOf(3600), responseCookie.maxAge());
        assertEquals("/secure", responseCookie.path());
        assertTrue(responseCookie.secure());
        assertTrue(responseCookie.httpOnly());
    }

    @Test
    public void testCopyCookieSameSitePolicies() {
        for (final Http.Cookie.SameSite policy : Http.Cookie.SameSite.values()) {
            final PlayWebContext context = new PlayWebContext(new Http.RequestBuilder().build());
            final Cookie cookie = new Cookie("token", "value");
            cookie.setSameSitePolicy(policy.value());
            cookie.setSecure(true);
            context.addResponseCookie(cookie);
            assertEquals(Optional.of(policy), context.supplementResponse(Results.ok()).cookie("token").orElseThrow().sameSite());
        }
    }

    @Test
    public void testCookieWithNoSameSitePolicy() {
        final PlayWebContext context = new PlayWebContext(new Http.RequestBuilder().build());
        context.addResponseCookie(new Cookie("token", "value"));
        assertTrue(context.supplementResponse(Results.ok()).cookie("token").orElseThrow().sameSite().isEmpty());
    }

    @Test
    public void testIpv6HostWithExplicitPort() {
        final PlayWebContext context = new PlayWebContext(new Http.RequestBuilder().host("[::1]:9000").build());
        assertEquals("[::1]", context.getServerName());
        assertEquals(9000, context.getServerPort());
    }

    @Test
    public void testIpv6HostWithDefaultPorts() {
        for (final boolean secure : new boolean[]{false, true}) {
            final PlayWebContext context = new PlayWebContext(new Http.RequestBuilder().host("[2001:db8::1]").secure(secure).build());
            assertEquals("[2001:db8::1]", context.getServerName());
            assertEquals(secure ? 443 : 80, context.getServerPort());
        }
    }

}
