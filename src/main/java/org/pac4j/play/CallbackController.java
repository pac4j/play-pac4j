package org.pac4j.play;

import org.pac4j.core.config.Config;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.play.store.PlaySessionStore;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * <p>This filter finishes the login process for an indirect client, based on the {@link #callbackLogic}.</p>
 *
 * <p>The configuration can be provided via setters: {@link #setDefaultUrl(String)} (default url after login if none was requested) and
 * {@link #setMultiProfile(boolean)} (whether multiple profiles should be kept).</p>
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @since 1.5.0
 */
public class CallbackController extends Controller {

    private CallbackLogic<Result, PlayWebContext> callbackLogic = new DefaultCallbackLogic<>();

    private String defaultUrl;

    private Boolean multiProfile;

    @Inject
    protected Config config;
    @Inject
    protected PlaySessionStore playSessionStore;

    public Result callback() {

        assertNotNull("callbackLogic", callbackLogic);

        assertNotNull("config", config);
        final PlayWebContext playWebContext = new PlayWebContext(ctx(), playSessionStore);

        return callbackLogic.perform(playWebContext, config, config.getHttpActionAdapter(), this.defaultUrl, this.multiProfile, false);
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }

    public void setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public boolean isMultiProfile() {
        return multiProfile;
    }

    public void setMultiProfile(boolean multiProfile) {
        this.multiProfile = multiProfile;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }
}
