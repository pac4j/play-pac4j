package org.pac4j.play;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.regex.Pattern;

import static org.pac4j.core.util.CommonHelper.*;

/**
 * <p>This filter handles the application logout process.</p>
 * <p>After logout, the user is redirected to the url defined by the <code>url</code> request parameter if it matches the <code>logoutUrlPattern</code>.
 * Or the user is redirected to the <code>defaultUrl</code> if it is defined. Otherwise, a blank page is displayed.</p>
 *
 * <p>The configuration can be provided via setters: {@link #setDefaultUrl(String)} and {@link #setLogoutUrlPattern(String)}.</p>
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
public class ApplicationLogoutController extends Controller {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String defaultUrl;

    protected String logoutUrlPattern = Pac4jConstants.DEFAULT_LOGOUT_URL_PATTERN_VALUE;

    @Inject
    protected Config config;

    public Result logout() {

        assertNotNull("config", config);
        assertNotBlank(Pac4jConstants.LOGOUT_URL_PATTERN, this.logoutUrlPattern);

        final WebContext context = new PlayWebContext(ctx(), config.getSessionStore());
        final ProfileManager manager = new ProfileManager(context);
        manager.logout();
        ctx().session().remove(Pac4jConstants.SESSION_ID);

        final String url = context.getRequestParameter(Pac4jConstants.URL);
        String redirectUrl = this.defaultUrl;
        if (url != null && Pattern.matches(this.logoutUrlPattern, url)) {
            redirectUrl = url;
        }
        logger.debug("redirectUrl: {}", redirectUrl);
        if (redirectUrl != null) {
            return redirect(redirectUrl);
        } else {
            return ok();
        }
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
}
