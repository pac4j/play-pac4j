package org.pac4j.play.http;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.adapter.HttpActionAdapter;

import org.pac4j.core.util.TestsConstants;

import org.pac4j.core.util.TestsHelper;
import org.pac4j.play.PlayWebContext;

import play.mvc.Result;
import play.test.Helpers;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link PlayHttpActionAdapter}.
 *
 * @author Jerome Leleu
 * @since 2.3.0
 */
public final class DefaultHttpActionAdapterTests implements TestsConstants {

    private HttpActionAdapter<Result, PlayWebContext> adapter;

    private PlayWebContext context;
    
    @Before
    public void setUp() {
        adapter = new PlayHttpActionAdapter();
        context = mock(PlayWebContext.class);
    }
    
    @After
    public void teardown() {
    }

    protected String getBody(final Result result) throws IOException {
      return Helpers.contentAsString(result);
    }

    @Test
    public void testUnauthorized() throws IOException {
        final Result result = adapter.adapt(HttpConstants.UNAUTHORIZED, null);
        assertEquals(401, result.status());
        assertEquals("authentication required", getBody(result));
    }

    @Test
    public void testForbidden() throws IOException {
        final Result result = adapter.adapt(HttpConstants.FORBIDDEN, null);
        assertEquals(403, result.status());
        assertEquals("forbidden", getBody(result));
    }

    @Test
    public void testRedirect() throws IOException {
        when(context.getLocation()).thenReturn(PAC4J_URL);
        final Result result = adapter.adapt(HttpConstants.TEMP_REDIRECT, context);
        assertEquals(303, result.status());
        assertEquals(PAC4J_URL, result.redirectLocation().get());
    }

    @Test
    public void testBadRequest() throws IOException {
        final Result result = adapter.adapt(HttpConstants.BAD_REQUEST, context);
        assertEquals(400, result.status());
        assertEquals("bad request", getBody(result));
    }

    @Test
    public void testOk() throws IOException {
        when(context.getResponseContent()).thenReturn(VALUE);
        final Result result = adapter.adapt(HttpConstants.OK, context);
        assertEquals(200, result.status());
        assertEquals(VALUE, getBody(result));
    }

    @Test
    public void testUnsupported() throws IOException {
        TestsHelper.expectException(() -> adapter.adapt(HttpConstants.CREATED, null), TechnicalException.class, "Unsupported HTTP action: " + HttpConstants.CREATED);
    }
}
