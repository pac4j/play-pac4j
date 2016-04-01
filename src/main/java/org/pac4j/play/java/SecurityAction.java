package org.pac4j.play.java;

import org.pac4j.core.authorization.checker.AuthorizationChecker;
import org.pac4j.core.authorization.checker.DefaultAuthorizationChecker;
import org.pac4j.core.client.*;
import org.pac4j.core.client.finder.ClientFinder;
import org.pac4j.core.client.finder.DefaultClientFinder;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

import org.pac4j.play.PlayWebContext;
import play.libs.F.Promise;
import play.mvc.Http.Context;
import play.mvc.Result;

import javax.inject.Inject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.pac4j.core.util.CommonHelper.*;

/**
 * <p>This action protects an url by checking that the user is authenticated and that the authorizations are checked, according to the clients and authorizers configuration.
 * If the user is not authenticated, it performs authentication for direct clients or starts the login process for indirect clients.</p>
 *
 * <p>The configuration can be provided via annotation parameters: <code>clients</code>, <code>authorizers</code> and <code>multiProfile</code>.</p>
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @since 1.0.0
 */
public class SecurityAction extends AbstractConfigAction {

    @Inject
    protected Config config;

    protected ClientFinder clientFinder = new DefaultClientFinder();

    protected AuthorizationChecker authorizationChecker = new DefaultAuthorizationChecker();

    public SecurityAction() {
    }

    public SecurityAction(final Config config) {
        this.config = config;
    }

    @Override
    public Promise<Result> call(final Context ctx) throws Throwable {

        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(configuration);
        final String clients = getStringParam(invocationHandler, CLIENTS_METHOD, null);
        final String authorizers = getStringParam(invocationHandler, AUTHORIZERS_METHOD, null);
        final boolean multiProfile = getBooleanParam(invocationHandler, MULTI_PROFILE_METHOD, false);

        return internalCall(ctx, clients, authorizers, multiProfile);
    }

    public Promise<Result> internalCall(final Context ctx, final String clients, final String authorizers, final boolean multiProfile) throws Throwable {

        assertNotNull("config", config);
        assertNotNull("config.httpActionAdapter", config.getHttpActionAdapter());
        final PlayWebContext context =  new PlayWebContext(ctx, config.getSessionStore());

        logger.debug("url: {}", context.getFullRequestURL());

        final Clients configClients = config.getClients();
        assertNotNull("configClients", configClients);
        logger.debug("clients: {}", clients);
        final List<Client> currentClients = clientFinder.find(configClients, context, clients);
        logger.debug("currentClients: {}", currentClients);

        final boolean useSession = useSession(context, currentClients);
        logger.debug("useSession: {}", useSession);

        final Promise<List<UserProfile>> promiseProfiles = Promise.promise(() -> {

            final ProfileManager manager = new ProfileManager(context);
            List<UserProfile> profiles = manager.getAll(useSession);
            logger.debug("profiles: {}", profiles);
            logger.debug("multiProfile: {}", multiProfile);

            // no profile and some current clients
            if (isEmpty(profiles) && isNotEmpty(currentClients)) {
                // loop on all clients searching direct ones to perform authentication
                for (final Client currentClient : currentClients) {
                    if (currentClient instanceof DirectClient) {
                        logger.debug("Performing authentication for client: {}", currentClient);

                        final Credentials credentials = currentClient.getCredentials(context);
                        final UserProfile profile = currentClient.getUserProfile(credentials, context);
                        logger.debug("profile: {}", profile);
                        if (profile != null) {
                            manager.save(useSession, profile, multiProfile);
                            if (!multiProfile) {
                                break;
                            }
                        }
                    }
                }
                profiles = manager.getAll(useSession);
                logger.debug("new profiles: {}", profiles);
            }

            return profiles;
        });

        return promiseProfiles.flatMap((profiles) -> {

            if (isNotEmpty(profiles)) {
                logger.debug("authorizers: {}", authorizers);
                if (authorizationChecker.isAuthorized(context, profiles, authorizers, config.getAuthorizers())) {
                    logger.debug("authenticated and authorized -> grant access");
                    // when called from Scala
                    if (delegate == null) {
                        return Promise.pure(null);
                    } else {
                        return delegate.call(ctx);
                    }
                } else {
                    logger.debug("forbidden");
                    return forbidden(context, currentClients, profiles, authorizers);
                }
            } else {
                if (startAuthentication(context, currentClients)) {
                    logger.debug("Starting authentication");
                    saveRequestedUrl(context, currentClients);
                    return Promise.promise(() -> redirectToIdentityProvider(context, currentClients));
                } else {
                    logger.debug("unauthorized");
                    return unauthorized(context, currentClients);
                }
            }
        }).recover(throwable -> {
            if (throwable instanceof RequiresHttpAction) {
                final RequiresHttpAction e = (RequiresHttpAction) throwable;
                return (Result) config.getHttpActionAdapter().adapt(e.getCode(), context);
            } else {
                throw new TechnicalException(throwable);
            }
        });
    }

    protected boolean useSession(final WebContext context, final List<Client> currentClients) {
        return isEmpty(currentClients) || currentClients.get(0) instanceof IndirectClient;
    }

    protected Promise<Result> forbidden(final PlayWebContext context, final List<Client> currentClients, final List<UserProfile> profiles, final String authorizers) {
        return Promise.pure((Result) config.getHttpActionAdapter().adapt(HttpConstants.FORBIDDEN, context));
    }

    protected boolean startAuthentication(final PlayWebContext context, final List<Client> currentClients) {
        return isNotEmpty(currentClients) && currentClients.get(0) instanceof IndirectClient;
    }

    protected void saveRequestedUrl(final WebContext context, final List<Client> currentClients) {
        final String requestedUrl = context.getFullRequestURL();
        logger.debug("requestedUrl: {}", requestedUrl);
        context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
    }

    protected Result redirectToIdentityProvider(final PlayWebContext context, final List<Client> currentClients) throws RequiresHttpAction {
        final IndirectClient currentClient = (IndirectClient) currentClients.get(0);
        currentClient.redirect(context);
        return (Result) config.getHttpActionAdapter().adapt(context.getResponseStatus(), context);
    }

    protected Promise<Result> unauthorized(final PlayWebContext context, final List<Client> currentClients) {
        return Promise.pure((Result) config.getHttpActionAdapter().adapt(HttpConstants.UNAUTHORIZED, context));
    }
}
