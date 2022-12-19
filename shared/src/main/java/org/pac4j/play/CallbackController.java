package org.pac4j.play;

import lombok.Getter;
import lombok.Setter;
import org.pac4j.core.adapter.FrameworkAdapter;
import org.pac4j.core.config.Config;
import org.pac4j.play.context.PlayFrameworkParameters;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * <p>This filter finishes the login process for an indirect client.</p>
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @since 1.5.0
 */
@Getter
@Setter
public class CallbackController extends Controller {

    private String defaultUrl;

    private Boolean renewSession;

    private String defaultClient;

    @Inject
    protected Config config;
    @Inject
    protected HttpExecutionContext ec;

    public CompletionStage<Result> callback(final Http.Request request) {

        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);

        return CompletableFuture.supplyAsync(() ->
                    (Result) config.getCallbackLogic().perform(config, defaultUrl, renewSession, defaultClient, new PlayFrameworkParameters(request))
               , ec.current());
    }
}
