package org.pac4j.play;

import org.pac4j.core.config.Config;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.play.store.PlaySessionStore;
import play.mvc.Controller;
import play.mvc.Result;
import play.libs.concurrent.HttpExecutionContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * <p>This filter handles the (application + identity provider) logout process, based on the {@link #logoutLogic}.</p>
 *
 * <p>The configuration can be provided via setters: {@link #setDefaultUrl(String)} (default logourl url), {@link #setLogoutUrlPattern(String)} (pattern that logout urls must match),
 * {@link #setLocalLogout(Boolean)} (whether the application logout must be performed), {@link #setDestroySession(Boolean)} (whether we must destroy the web session during the local logout)
 * and {@link #setCentralLogout(Boolean)} (whether the centralLogout must be performed).</p>
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
public class LogoutController extends Controller {

    private LogoutLogic<Result, PlayWebContext> logoutLogic = new DefaultLogoutLogic<>();

    private String defaultUrl;

    private String logoutUrlPattern;

    private Boolean localLogout;

    private Boolean destroySession;

    private Boolean centralLogout;

    @Inject
    protected Config config;
    @Inject
    protected PlaySessionStore playSessionStore;
    @Inject
    protected HttpExecutionContext ec;

    public CompletionStage<Result> logout() {

        assertNotNull("logoutLogic", logoutLogic);

        assertNotNull("config", config);
        final PlayWebContext playWebContext = new PlayWebContext(ctx(), playSessionStore);

        return CompletableFuture.supplyAsync(() -> logoutLogic.perform(playWebContext, config, config.getHttpActionAdapter(), this.defaultUrl,
                this.logoutUrlPattern, this.localLogout, this.destroySession, this.centralLogout), ec.current());
    }

    public String getDefaultUrl() {
        return this.defaultUrl;
    }

    public void setDefaultUrl(final String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public String getLogoutUrlPattern() {
        return logoutUrlPattern;
    }

    public void setLogoutUrlPattern(final String logoutUrlPattern) {
        this.logoutUrlPattern = logoutUrlPattern;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(final Config config) {
        this.config = config;
    }

    public Boolean getLocalLogout() {
        return localLogout;
    }

    public void setLocalLogout(final Boolean localLogout) {
        this.localLogout = localLogout;
    }

    public Boolean getDestroySession() {
        return destroySession;
    }

    public void setDestroySession(final Boolean destroySession) {
        this.destroySession = destroySession;
    }

    public Boolean getCentralLogout() {
        return centralLogout;
    }

    public void setCentralLogout(final Boolean centralLogout) {
        this.centralLogout = centralLogout;
    }

    /**
     * Please be aware that {@link Config#getLogoutLogic()} ()} of {@linkplain #config} field is ignored because it uses
     * raw types instead of generics. We would like to avoid runtime type casts.
     */
    public void setCallbackLogic(final LogoutLogic<Result, PlayWebContext> logoutLogic) {
        this.logoutLogic = logoutLogic;
    }
}
