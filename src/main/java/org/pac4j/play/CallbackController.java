package org.pac4j.play;

import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Result;
import org.pac4j.core.config.Config;

import javax.inject.Inject;

import static org.pac4j.core.util.CommonHelper.*;

/**
 * <p>This controller finishes the login process for an indirect client.</p>
 *
 * <p>The configuration can be provided via setters: {@link #setDefaultUrl(String)} (default url after login if none was requested) and
 * {@link #setMultiProfile(boolean)} (whether multiple profiles should be kept).</p>
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @since 1.5.0
 */
public class CallbackController extends Controller {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;

    protected boolean multiProfile;

    @Inject
    protected Config config;

    public Result callback() {

        assertNotNull("config", config);
        assertNotNull("config.httpActionAdapter", config.getHttpActionAdapter());
        final PlayWebContext context = new PlayWebContext(ctx(), config.getSessionStore());

        final Clients clients = config.getClients();
        assertNotNull("clients", clients);
        final Client client = clients.findClient(context);
        logger.debug("client: {}", client);
        assertNotNull("client", client);
        assertTrue(client instanceof IndirectClient, "only indirect clients are allowed on the callback url");

        try {
            final Credentials credentials = client.getCredentials(context);
            logger.debug("credentials: {}", credentials);

            final UserProfile profile = client.getUserProfile(credentials, context);
            logger.debug("profile: {}", profile);
            saveUserProfile(context, profile);
            return redirectToOriginallyRequestedUrl(context);

        } catch (final RequiresHttpAction e) {
            return (Result) config.getHttpActionAdapter().adapt(e.getCode(), context);
        }
    }

    protected void saveUserProfile(final WebContext context, final UserProfile profile) {
        final ProfileManager manager = new ProfileManager(context);
        if (profile != null) {
            manager.save(true, profile, this.multiProfile);
        }
    }

    protected Result redirectToOriginallyRequestedUrl(final WebContext context) {
        final String requestedUrl = (String) context.getSessionAttribute(Pac4jConstants.REQUESTED_URL);
        logger.debug("requestedUrl: {}", requestedUrl);
        if (isNotBlank(requestedUrl)) {
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

    public boolean isMultiProfile() {
        return multiProfile;
    }

    public void setMultiProfile(boolean multiProfile) {
        this.multiProfile = multiProfile;
    }
}
