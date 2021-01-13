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

import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link PlayHttpActionAdapter}.
 *
 * @author Jerome Leleu
 * @since 2.3.0
 */
public final class PlayHttpActionAdapterTests implements TestsConstants {

    private HttpActionAdapter adapter;

    private PlayWebContext context;
    
    @Before
    public void setUp() {
        adapter = new PlayHttpActionAdapter();
        context = new PlayWebContext(mock(Http.RequestHeader.class));
    }
    
    @After
    public void teardown() {
    }

    protected String getBody(final Result result) {
      return Helpers.contentAsString(result);
    }

    @Test
    public void testUnauthorized() {
        final Result result = (Result) adapter.adapt(new StatusAction(HttpConstants.UNAUTHORIZED), context);
        assertEquals(401, result.status());
    }

    @Test
    public void testForbidden() {
        final Result result = (Result) adapter.adapt(new StatusAction(HttpConstants.FORBIDDEN), context);
        assertEquals(403, result.status());
    }

    @Test
    public void testRedirectFound() {
        final Result result = (Result) adapter.adapt(new FoundAction(PAC4J_URL), context);
        assertEquals(HttpConstants.FOUND, result.status());
        assertEquals(PAC4J_URL, result.redirectLocation().get());
    }

    @Test
    public void testRedirectSeeOther() {
        final Result result = (Result) adapter.adapt(new SeeOtherAction(PAC4J_URL), context);
        assertEquals(HttpConstants.SEE_OTHER, result.status());
        assertEquals(PAC4J_URL, result.redirectLocation().get());
    }

    @Test
    public void testBadRequest() {
        final Result result = (Result) adapter.adapt(new StatusAction(HttpConstants.BAD_REQUEST), context);
        assertEquals(400, result.status());
    }

    @Test
    public void testOk() {
        final Result result = (Result) adapter.adapt(new OkAction(VALUE), context);
        assertEquals(200, result.status());
        assertEquals(VALUE, getBody(result));
    }

    @Test
    public void testNoActionProvided() {
        TestsHelper.expectException(() -> adapter.adapt(null, null), TechnicalException.class, "No action provided");
    }
}
