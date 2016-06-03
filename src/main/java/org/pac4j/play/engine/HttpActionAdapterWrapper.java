package org.pac4j.play.engine;

import org.pac4j.play.PlayWebContext;
import play.libs.F.Promise;
import org.pac4j.core.http.HttpActionAdapter;
import play.mvc.Result;

/**
 * Wrapper of a <code>HttpActionAdapter&lt;Result,PlayWebContext&gt;</code>.
 *
 * @author Jerome Leleu
 * @since 2.3.0
 */
public final class HttpActionAdapterWrapper implements HttpActionAdapter<Promise<Result>, PlayWebContext> {

    private final HttpActionAdapter<Result, PlayWebContext> wrapped;

    public HttpActionAdapterWrapper(final HttpActionAdapter<Result, PlayWebContext> wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Promise<Result> adapt(final int code, final PlayWebContext context) {
        return Promise.pure(wrapped.adapt(code, context));
    }
}
