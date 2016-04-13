package org.pac4j.play;

import org.junit.Before;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.MockSessionStore;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.util.TestsConstants;
import play.core.j.JavaResultExtractor;
import play.mvc.Http;
import play.mvc.Result;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility class for tests.
 *
 * @author Jerome Leleu
 * @since 2.2.0
 */
public abstract class AbstractWebTests implements TestsConstants {

    protected String getBody(final Result result) throws IOException {
        return new String(JavaResultExtractor.getBody(result, 0L), HttpConstants.UTF8_ENCODING);
    }

    protected Http.Context ctx;

    protected WebContext webContext;

    protected Map<String, String[]> requestParameters;

    protected Config config;

    @Before
    public void setUp() {
        final SessionStore sessionStore = new MockSessionStore();
        config = new Config();
        config.setSessionStore(sessionStore);
        final play.api.mvc.RequestHeader header = mock(play.api.mvc.RequestHeader.class);
        final Http.Request request = mock(Http.Request.class);
        requestParameters = new HashMap<>();
        when(request.queryString()).thenReturn(requestParameters);
        final Map<String, String> flashData = new HashMap<>();
        final Map<String, Object> argData = new HashMap<>();
        ctx = new Http.Context(1L, header, request, flashData, flashData, argData);
        Http.Context.current.set(ctx);
        webContext = new PlayWebContext(ctx, sessionStore);
    }
}
