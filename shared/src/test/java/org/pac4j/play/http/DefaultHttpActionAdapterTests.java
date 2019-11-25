package org.pac4j.play.http;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.exception.http.*;
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
        final Result result = adapter.adapt(new StatusAction(HttpConstants.UNAUTHORIZED), null);
        assertEquals(401, result.status());
    }

    @Test
    public void testForbidden() throws IOException {
        final Result result = adapter.adapt(new StatusAction(HttpConstants.FORBIDDEN), null);
        assertEquals(403, result.status());
    }

    @Test
    public void testRedirectFound() throws IOException {
        when(context.getLocation()).thenReturn(PAC4J_URL);
        final Result result = adapter.adapt(new FoundAction(PAC4J_URL), context);
        assertEquals(HttpConstants.FOUND, result.status());
        assertEquals(PAC4J_URL, result.redirectLocation().get());
    }

    @Test
    public void testRedirectSeeOther() throws IOException {
        when(context.getLocation()).thenReturn(PAC4J_URL);
        final Result result = adapter.adapt(new SeeOtherAction(PAC4J_URL), context);
        assertEquals(HttpConstants.SEE_OTHER, result.status());
        assertEquals(PAC4J_URL, result.redirectLocation().get());
    }

    @Test
    public void testBadRequest() throws IOException {
        final Result result = adapter.adapt(new StatusAction(HttpConstants.BAD_REQUEST), context);
        assertEquals(400, result.status());
    }

    @Test
    public void testOk() throws IOException {
        when(context.getResponseContent()).thenReturn(VALUE);
        final Result result = adapter.adapt(new OkAction(VALUE), context);
        assertEquals(200, result.status());
        assertEquals(VALUE, getBody(result));
    }

    @Test
    public void testNoActionProvided() throws IOException {
        TestsHelper.expectException(() -> adapter.adapt(null, null), TechnicalException.class, "No action provided");
    }
}
