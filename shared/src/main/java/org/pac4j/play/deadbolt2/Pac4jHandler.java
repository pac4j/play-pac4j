package org.pac4j.play.deadbolt2;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Subject;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.DirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.StatusAction;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.play.PlayWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.pac4j.core.util.CommonHelper.isNotEmpty;

/**
 * This is the deadbolt handler for pac4j: the deadbolt subject is built from the pac4j user profile.
 * If no pac4j profile exists, the user is redirected to the identity provider for login for indirect clients; otherwise, a 401 error is returned.
 * If the subject is not allowed, a 403 error is returned.
 *
 * @author Jerome Leleu
 * @since 2.6.0
 */
public class Pac4jHandler extends DefaultSecurityLogic implements DeadboltHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pac4jHandler.class);

    private final Config config;

    private final HttpExecutionContext httpExecutionContext;

    private final String clients;

    private final SessionStore sessionStore;

    private final Pac4jRoleHandler rolePermissionsHandler;

    public Pac4jHandler(final Config config, final HttpExecutionContext httpExecutionContext, final String clients, final SessionStore sessionStore, final Pac4jRoleHandler rolePermissionsHandler) {
        CommonHelper.assertNotNull("config", config);
        CommonHelper.assertNotNull("httpExecutionContext", httpExecutionContext);
        CommonHelper.assertNotNull("playSessionStore", sessionStore);

        this.config = config;
        this.httpExecutionContext = httpExecutionContext;
        this.clients = clients;
        this.sessionStore = sessionStore;
        this.rolePermissionsHandler = rolePermissionsHandler;
    }

    @Override
    public long getId() {
        return clients.hashCode();
    }

    @Override
    public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.RequestHeader requestHeader, final Optional<String> content) {
        return CompletableFuture.supplyAsync(() -> {
            final Optional<UserProfile> profile = getProfile(requestHeader);
            if (profile.isPresent()) {
                LOGGER.debug("profile found -> returning empty");
                return Optional.empty();
            } else {
                final PlayWebContext playWebContext = new PlayWebContext(requestHeader);
                final HttpActionAdapter httpActionAdapter = config.getHttpActionAdapter();
                final List<Client> currentClients = getClientFinder().find(config.getClients(), playWebContext, clients);
                LOGGER.debug("currentClients: {}", currentClients);

                HttpAction action;
                try {
                    if (startDirectAuthentication(currentClients)) {
                        LOGGER.debug("Starting direct authentication");
                        DirectClient client = (DirectClient) currentClients.get(0);
                        Optional<Credentials> credentials = client.getCredentials(playWebContext, sessionStore);
                        if (credentials.isPresent()) {
                            UserProfile userProfile = credentials.get().getUserProfile();
                            if (userProfile != null) {
                                setProfile(requestHeader, userProfile);
                                return Optional.empty();
                            }
                        }
                        LOGGER.debug("unauthorized");
                        action = unauthorized(playWebContext, sessionStore, currentClients);
                    } else if (startAuthentication(playWebContext, sessionStore, currentClients)) {
                        LOGGER.debug("Starting authentication");
                        saveRequestedUrl(playWebContext, sessionStore, currentClients, null);
                        action = redirectToIdentityProvider(playWebContext, sessionStore, currentClients);
                    } else {
                        LOGGER.debug("unauthorized");
                        action = unauthorized(playWebContext, sessionStore, currentClients);
                    }
                } catch (final HttpAction e) {
                    action = e;
                }
                return Optional.of((Result) httpActionAdapter.adapt(action, playWebContext));
            }
        }, httpExecutionContext.current());
    }

    @Override
    public CompletionStage<Optional<? extends Subject>> getSubject(final Http.RequestHeader requestHeader) {
        return CompletableFuture.supplyAsync(() -> {
            final Optional<UserProfile> profile = getProfile(requestHeader);
            if (profile.isPresent()) {
                LOGGER.debug("profile found: {} -> building a subject", profile);
                return Optional.of(new Pac4jSubject(profile.get()));
            } else {
                LOGGER.debug("no profile found -> returning empty");
                return Optional.empty();
            }
        }, httpExecutionContext.current());
    }

    @Override
    public CompletionStage<List<? extends Permission>> getPermissionsForRole(String roleName) {
        return rolePermissionsHandler.getPermissionsForRole(clients, roleName, httpExecutionContext);
    }

    private Optional<UserProfile> getProfile(final Http.RequestHeader requestHeader) {
        final PlayWebContext playWebContext = new PlayWebContext(requestHeader);
        final ProfileManager manager = new ProfileManager(playWebContext, sessionStore);
        return manager.getProfile();
    }

    private void setProfile(final Http.RequestHeader requestHeader, UserProfile profile) {
        final PlayWebContext playWebContext = new PlayWebContext(requestHeader);
        playWebContext.setRequestAttribute(Pac4jConstants.USER_PROFILES, profile);
    }

    @Override
    public CompletionStage<Result> onAuthFailure(final Http.RequestHeader requestHeader, final Optional<String> content) {
        return CompletableFuture.supplyAsync(() -> {
            final PlayWebContext playWebContext = new PlayWebContext(requestHeader);
            final HttpActionAdapter httpActionAdapter = config.getHttpActionAdapter();
            return (Result) httpActionAdapter.adapt(new StatusAction(HttpConstants.FORBIDDEN), playWebContext);
        }, httpExecutionContext.current());
    }

    @Override
    public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.RequestHeader requestHeader) {
        throw new TechnicalException("getDynamicResourceHandler() not supported in Pac4jHandler");
    }

    private boolean startDirectAuthentication(final List<Client> currentClients) {
        return isNotEmpty(currentClients) && currentClients.get(0) instanceof DirectClient;
    }
}
