package org.pac4j.play.engine;

import org.pac4j.play.PlayWebContext;
import org.pac4j.core.http.HttpActionAdapter;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


/**
 * Wrapper of a <code>HttpActionAdapter&lt;Result,PlayWebContext&gt;</code>.
 *
 * @author Jerome Leleu
 * @since 2.3.0
 */
public final class HttpActionAdapterWrapper implements HttpActionAdapter<CompletionStage<Result>, PlayWebContext> {

    private final HttpActionAdapter<Result, PlayWebContext> wrapped;

    public HttpActionAdapterWrapper(final HttpActionAdapter<Result, PlayWebContext> wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public CompletionStage<Result> adapt(final int code, final PlayWebContext context) {
        return CompletableFuture.completedFuture(wrapped.adapt(code, context));
    }
}
