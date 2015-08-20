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

import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.play.handler.HttpActionHandler;
import org.pac4j.play.store.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Result;
import org.pac4j.core.config.Config;

import javax.inject.Inject;

/**
 * <p>This controller handles the callback from the identity provider to finish the authentication process.</p>
 * <p>The default url after login (if none has originally be requested) can be defined via the {@link #setDefaultUrl(String)} method.</p>
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @since 1.5.0
 */
public class CallbackController extends Controller {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;

    @Inject
    protected Config config;

    @Inject
    protected HttpActionHandler httpActionHandler;

    @Inject
    protected DataStore dataStore;

    public Result callback() {

        CommonHelper.assertNotNull("config", config);
        final Clients clients = config.getClients();
        CommonHelper.assertNotNull("clients", clients);
        CommonHelper.assertNotBlank(Pac4jConstants.DEFAULT_URL, this.defaultUrl);

        final PlayWebContext context = new PlayWebContext(ctx(), dataStore);
        final ProfileManager manager = new ProfileManager(context);
        final Client client = clients.findClient(context);
        logger.debug("client: {}", client);
        CommonHelper.assertNotNull("client", client);

        final Credentials credentials;
        try {
            credentials = client.getCredentials(context);
        } catch (final RequiresHttpAction e) {
            return httpActionHandler.handle(e.getCode(), context);
        }
        logger.debug("credentials: {}", credentials);

        final CommonProfile profile = (CommonProfile) client.getUserProfile(credentials, context);
        logger.debug("profile: {}", profile);
        if (profile != null) {
            manager.save(true, profile);
        }

        return redirectToOriginallyRequestedUrl(context);
    }

    protected Result redirectToOriginallyRequestedUrl(final WebContext context) {
        final String requestedUrl = (String) context.getSessionAttribute(Pac4jConstants.REQUESTED_URL);
        logger.debug("requestedUrl: {}", requestedUrl);
        if (CommonHelper.isNotBlank(requestedUrl)) {
            context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, null);
            return redirect(requestedUrl);
        } else {
            return redirect(this.defaultUrl);
        }
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }

    public void setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }
}
