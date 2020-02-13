package org.pac4j.play;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import org.pac4j.core.util.TestsConstants;
import org.pac4j.play.store.PlaySessionStore;

import play.mvc.Http.Request;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Tests {@link PlayWebContext}.
 *
 * @author Ivan Suhinin
 * @since 3.0.0
 */
@RunWith(PowerMockRunner.class)
public final class PlayWebContextTests implements TestsConstants {

    private Request requestMock;
    private PlayWebContext webContext;

    private static final String domainWithoutPort = "somedomain.com";
    
    @Before
    public void setUp() {
        PlaySessionStore sessionStoreMock = mock(PlaySessionStore.class);
        requestMock = mock(Request.class);
        webContext = new PlayWebContext(requestMock, sessionStoreMock);
    }
    
    @After
    public void teardown() {
    }

    @Test
    public void testRandomNonSecureServerPort() throws IOException {
        final Integer port = 9000;
        String host = domainWithoutPort + ":" + port.toString();

        PowerMockito.when(requestMock.secure()).thenReturn(false);
        PowerMockito.when(requestMock.host()).thenReturn(host);

        assertEquals(port.intValue(), webContext.getServerPort());
    }

    @Test
    public void testRandomSecureServerPort() throws IOException {
        final Integer port = 9000;
        String host = domainWithoutPort + ":" + port.toString();

        PowerMockito.when(requestMock.secure()).thenReturn(true);
        PowerMockito.when(requestMock.host()).thenReturn(host);

        assertEquals(port.intValue(), webContext.getServerPort());
    }

    @Test
    public void testNonSecureServerPort() throws IOException {
        PowerMockito.when(requestMock.secure()).thenReturn(false);
        PowerMockito.when(requestMock.host()).thenReturn(domainWithoutPort);

        assertEquals(80, webContext.getServerPort());
    }

    @Test
    public void testSecureServerPort() throws IOException {
        PowerMockito.when(requestMock.secure()).thenReturn(true);
        PowerMockito.when(requestMock.host()).thenReturn(domainWithoutPort);

        assertEquals(443, webContext.getServerPort());
    }
}
