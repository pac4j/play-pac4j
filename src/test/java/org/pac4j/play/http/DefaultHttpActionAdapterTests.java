package org.pac4j.play.http;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.http.HttpActionAdapter;
import play.mvc.Result;

import static org.junit.Assert.*;

/**
 * Tests {@link DefaultHttpActionAdapter}.
 *
 * @author Jerome Leleu
 * @since 2.2.0
 */
public final class DefaultHttpActionAdapterTests {

    private HttpActionAdapter adapter;

    @Before
    public void setUp() {
        adapter = new DefaultHttpActionAdapter();
    }

    @Test
    public void testUnauthorized() {
        final Result result = (Result) adapter.adapt(HttpConstants.UNAUTHORIZED, null);
        assertEquals(401, result.status());
    }
}
