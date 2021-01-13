package org.pac4j.play;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.FindBest;
import org.pac4j.play.context.PlayContextFactory;
import org.pac4j.play.http.PlayHttpActionAdapter;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.libs.concurrent.HttpExecutionContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

/**
 * <p>This filter finishes the login process for an indirect client.</p>
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @since 1.5.0
 */
public class CallbackController extends Controller {

    private CallbackLogic callbackLogic;

    private String defaultUrl;

    private Boolean renewSession;

    private String defaultClient;

    @Inject
    protected Config config;
    @Inject
    protected SessionStore sessionStore;
    @Inject
    protected HttpExecutionContext ec;

    public CompletionStage<Result> callback(final Http.Request request) {

        final HttpActionAdapter bestAdapter = FindBest.httpActionAdapter(null, config, PlayHttpActionAdapter.INSTANCE);
        final CallbackLogic bestLogic = FindBest.callbackLogic(callbackLogic, config, DefaultCallbackLogic.INSTANCE);

        final WebContext context = FindBest.webContextFactory(null, config, PlayContextFactory.INSTANCE).newContext(request);

        return CompletableFuture.supplyAsync(() -> (Result) bestLogic.perform(context, sessionStore, config, bestAdapter,
                this.defaultUrl, this.renewSession, this.defaultClient), ec.current());
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }

    public void setDefaultUrl(final String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public Boolean getRenewSession() {
        return renewSession;
    }

    public void setRenewSession(final Boolean renewSession) {
        this.renewSession = renewSession;
    }

    public String getDefaultClient() {
        return defaultClient;
    }

    public void setDefaultClient(final String defaultClient) {
        this.defaultClient = defaultClient;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(final Config config) {
        this.config = config;
    }

    public CallbackLogic getCallbackLogic() {
        return callbackLogic;
    }

    public SessionStore getSessionStore() {
        return sessionStore;
    }

    public void setCallbackLogic(final CallbackLogic callbackLogic) {
        this.callbackLogic = callbackLogic;
    }
}
