package org.pac4j.play;

import org.pac4j.core.config.Config;
import org.pac4j.core.engine.ApplicationLogoutLogic;
import org.pac4j.play.engine.PlayApplicationLogoutLogic;
import org.pac4j.play.store.PlaySessionStore;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * <p>This filter handles the application logout process, based on the {@link #applicationLogoutLogic}.</p>
 *
 * <p>The configuration can be provided via setters: {@link #setDefaultUrl(String)} (default logourl url) and {@link #setLogoutUrlPattern(String)} (pattern that logout urls must match).</p>
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
public class ApplicationLogoutController extends Controller {

    private ApplicationLogoutLogic<Result, PlayWebContext> applicationLogoutLogic = new PlayApplicationLogoutLogic();

    private String defaultUrl;

    private String logoutUrlPattern;

    @Inject
    protected Config config;
    @Inject
    protected PlaySessionStore playSessionStore;

    public Result logout() {

        assertNotNull("applicationLogoutLogic", applicationLogoutLogic);

        assertNotNull("config", config);
        final PlayWebContext playWebContext = new PlayWebContext(ctx(), playSessionStore);

        return applicationLogoutLogic.perform(playWebContext, config, config.getHttpActionAdapter(), this.defaultUrl, this.logoutUrlPattern);
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

    public void setLogoutUrlPattern(String logoutUrlPattern) {
        this.logoutUrlPattern = logoutUrlPattern;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public ApplicationLogoutLogic<Result, PlayWebContext> getApplicationLogoutLogic() {
        return applicationLogoutLogic;
    }

    public void setApplicationLogoutLogic(ApplicationLogoutLogic<Result, PlayWebContext> applicationLogoutLogic) {
        this.applicationLogoutLogic = applicationLogoutLogic;
    }
}
