package org.pac4j.play;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.pac4j.core.util.TestsConstants;

import play.mvc.Http.Request;

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
}
