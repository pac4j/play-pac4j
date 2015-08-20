/*
  Copyright 2012 - 2015 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.play;

import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.play.store.DataStore;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.regex.Pattern;

/**
 * <p>This controller handles the application logout process.</p>
 * <p>After logout, the user is redirected to the url defined by the <i>url</i> parameter. If no url is provided, a blank page is displayed. If the url does not match the pattern, the default url is used.</p>
 * <p>The default url can be defined via the {@link #setDefaultUrl(String)} method.</p>
 * <p>The logout url pattern can be defined via the {@link #setLogoutUrlPattern(String)} method.</p>
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
public class ApplicationLogoutController extends Controller {

    protected String defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;

    protected String logoutUrlPattern = Pac4jConstants.DEFAULT_LOGOUT_URL_PATTERN_VALUE;

    @Inject
    protected DataStore dataStore;

    public Result logout() {

        CommonHelper.assertNotBlank(Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        CommonHelper.assertNotBlank(Pac4jConstants.LOGOUT_URL_PATTERN, this.logoutUrlPattern);

        final WebContext context = new PlayWebContext(ctx(), dataStore);
        final ProfileManager manager = new ProfileManager(context);
        manager.logout();

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
