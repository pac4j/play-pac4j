package org.pac4j.play.engine;

import org.pac4j.play.PlayWebContext;
import org.pac4j.core.http.HttpActionAdapter;
import play.mvc.Result;
import play.libs.concurrent.HttpExecutionContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.google.inject.Inject;

/**
 * Wrapper of a <code>HttpActionAdapter&lt;Result,PlayWebContext&gt;</code>.
 *
 * @author Jerome Leleu
 * @since 2.3.0
 */
public final class HttpActionAdapterWrapper implements HttpActionAdapter<CompletionStage<Result>, PlayWebContext> {
	
   private final HttpActionAdapter<Result, PlayWebContext> wrapped;
	
   @Inject
   private HttpExecutionContext ec;

   public HttpActionAdapterWrapper(final HttpActionAdapter<Result, PlayWebContext> wrapped) {
       this.wrapped = wrapped;
   }

   @Override
   public CompletionStage<Result> adapt(final int code, PlayWebContext context) {
       return CompletableFuture.supplyAsync(() -> wrapped.adapt(code, context), ec.current());
   }
}
