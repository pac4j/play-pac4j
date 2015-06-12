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

import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.play.java.ActionContext;
import org.pac4j.play.java.RequiresAuthenticationAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Map;

/**
 * <p>This controller is the class to finish the authentication process and logout the user.</p>
 * <p>Public methods : {@link #callback()}, {@link #logoutAndOk()} and {@link #logoutAndRedirect()} must be used in the routes file.</p>
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class SecurityCallbackController extends Controller {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityCallbackController.class);

    private RequiresAuthenticationAction action = new RequiresAuthenticationAction() {

        @Override
        protected Promise<CommonProfile> retrieveUserProfile(ActionContext actionContext) {
            return super.authenticate(actionContext);
        }

        @Override
        protected Promise<Result> authenticationSuccess(CommonProfile profile, ActionContext actionContext) {
            return redirectToTarget(actionContext);
        }

        @Override
        protected Promise<Result> authenticationFailure(ActionContext actionContext) {
            return redirectToTarget(actionContext);
        }

        private Promise<Result> redirectToTarget(final ActionContext actionContext) {
            // retrieve saved request and redirect
            return Promise.promise(new Function0<Result>() {
                @Override
                public Result apply() {
                    return redirect(defaultUrl(retrieveOriginalUrl(actionContext), Config.getDefaultSuccessUrl()));
                }
            });
        }

    };

    /**
     * This method handles the callback call from the provider to finish the authentication process. The credentials and then the profile of
     * the authenticated user is retrieved and the originally requested url (or the specific saved url) is restored.
     * 
     * @return the redirection to the saved request
     */
    public Promise<Result> callback() {

        return action.call(ctx());

    }

    /**
     * This method logouts the authenticated user.
     */
    private void logout() {
        // get the session id
        final String sessionId = session(Pac4jConstants.SESSION_ID);
        LOGGER.debug("sessionId for logout : {}", sessionId);
        if (StringUtils.isNotBlank(sessionId)) {
            // remove user profile from cache
            StorageHelper.removeProfile(sessionId);
            LOGGER.debug("remove user profile for sessionId : {}", sessionId);
        }
        session().remove(Pac4jConstants.SESSION_ID);
    }

    /**
     * This method logouts the authenticated user and send him to a blank page.
     * 
     * @return the redirection to the blank page
     */
    public Result logoutAndOk() {
        logout();
        return ok();
    }

    /**
     * This method logouts the authenticated user and send him to the url defined in the
     * {@link Constants#REDIRECT_URL_LOGOUT_PARAMETER_NAME} parameter name or to the <code>defaultLogoutUrl</code>.
     * This parameter is matched against the {@link Config#getLogoutUrlPattern()}.
     * 
     * @return the redirection to the "logout url"
     */
    public Result logoutAndRedirect() {
        logout();
        // parameters in url
        final Map<String, String[]> parameters = request().queryString();
        final String[] values = parameters.get(Constants.REDIRECT_URL_LOGOUT_PARAMETER_NAME);
        String value = null;
        if (values != null && values.length == 1) {
            String value0 = values[0];
            // check the url pattern
            if (Config.getLogoutUrlPattern().matcher(value0).matches()) {
                value = value0;
            }
        }
        return redirect(defaultUrl(value, Config.getDefaultLogoutUrl()));
    }

    /**
     * This method returns the default url from a specified url compared with a default url.
     * 
     * @param url
     * @param defaultUrl
     * @return the default url
     */
    public String defaultUrl(final String url, final String defaultUrl) {
        String redirectUrl = defaultUrl;
        if (StringUtils.isNotBlank(url)) {
            redirectUrl = url;
        }
        LOGGER.debug("defaultUrl : {}", redirectUrl);
        return redirectUrl;
    }
}
