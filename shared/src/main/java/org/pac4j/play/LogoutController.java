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
 * <p>This filter handles the (application + identity provider) logout process.</p>
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
@Getter
@Setter
public class LogoutController extends Controller {

    private String defaultUrl;

    private String logoutUrlPattern;

    private Boolean localLogout;

    private Boolean destroySession;

    private Boolean centralLogout;

    @Inject
    protected Config config;
    @Inject
    protected HttpExecutionContext ec;

    public CompletionStage<Result> logout(final Http.Request request) {

        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);

        return CompletableFuture.supplyAsync(() ->
                    (Result) config.getLogoutLogic().perform(config, defaultUrl, logoutUrlPattern, localLogout,
                            destroySession, centralLogout, new PlayFrameworkParameters(request))
                , ec.current());
    }
}
