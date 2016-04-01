package org.pac4j.play;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.regex.Pattern;

import static org.pac4j.core.util.CommonHelper.*;

/**
 * <p>This filter handles the application logout process.</p>
 * <p>After logout, the user is redirected to the url defined by the <code>url</code> request parameter. If no url is provided, a blank page is displayed.
 * If the <code>url</code> does not match the <code>logoutUrlPattern</code>, the <code>defaultUrl</code> is used.</p>
 *
 * <p>The configuration can be provided via setters: {@link #setDefaultUrl(String)} and {@link #setLogoutUrlPattern(String)}.</p>
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
public class ApplicationLogoutController extends Controller {

    protected String defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;

    protected String logoutUrlPattern = Pac4jConstants.DEFAULT_LOGOUT_URL_PATTERN_VALUE;

    @Inject
    protected Config config;

    public Result logout() {

        assertNotBlank(Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        assertNotBlank(Pac4jConstants.LOGOUT_URL_PATTERN, this.logoutUrlPattern);

        final WebContext context = new PlayWebContext(ctx(), config.getSessionStore());
        final ProfileManager manager = new ProfileManager(context);
        manager.logout();
        ctx().session().remove(Pac4jConstants.SESSION_ID);

        final String url = context.getRequestParameter(Pac4jConstants.URL);
        if (url == null) {
            return ok();
        } else {
            if (Pattern.matches(this.logoutUrlPattern, url)) {
                return redirect(url);
            } else {
                return redirect(this.defaultUrl);
            }
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
