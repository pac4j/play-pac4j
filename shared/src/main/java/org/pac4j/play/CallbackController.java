package org.pac4j.play;

import org.pac4j.core.config.Config;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.FindBest;
import org.pac4j.play.http.PlayHttpActionAdapter;
import org.pac4j.play.store.PlaySessionStore;
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

    private CallbackLogic<Result, PlayWebContext> callbackLogic;

    private String defaultUrl;

    private Boolean saveInSession;

    private Boolean multiProfile;

    private Boolean renewSession;

    private String defaultClient;

    @Inject
    protected Config config;
    @Inject
    protected PlaySessionStore playSessionStore;
    @Inject
    protected HttpExecutionContext ec;

    public CompletionStage<Result> callback(final Http.Request request) {

        final HttpActionAdapter<Result, PlayWebContext> bestAdapter = FindBest.httpActionAdapter(null, config, PlayHttpActionAdapter.INSTANCE);
        final CallbackLogic<Result, PlayWebContext> bestLogic = FindBest.callbackLogic(callbackLogic, config, DefaultCallbackLogic.INSTANCE);

        final PlayWebContext playWebContext = new PlayWebContext(request, playSessionStore);
        return CompletableFuture.supplyAsync(() -> bestLogic.perform(playWebContext, config, bestAdapter,
                this.defaultUrl, this.saveInSession, this.multiProfile, this.renewSession, this.defaultClient), ec.current());
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }

    public void setDefaultUrl(final String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public Boolean getSaveInSession() {
        return saveInSession;
    }

    public void setSaveInSession(final Boolean saveInSession) {
        this.saveInSession = saveInSession;
    }

    public boolean isMultiProfile() {
        return multiProfile;
    }

    public void setMultiProfile(final boolean multiProfile) {
        this.multiProfile = multiProfile;
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

    public CallbackLogic<Result, PlayWebContext> getCallbackLogic() {
        return callbackLogic;
    }

    public Boolean getMultiProfile() {
        return multiProfile;
    }

    public PlaySessionStore getPlaySessionStore() {
        return playSessionStore;
    }

    public void setCallbackLogic(final CallbackLogic<Result, PlayWebContext> callbackLogic) {
        this.callbackLogic = callbackLogic;
    }
}
