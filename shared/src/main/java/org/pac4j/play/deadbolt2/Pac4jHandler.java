package org.pac4j.play.deadbolt2;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Subject;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.DirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.store.PlaySessionStore;
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
public class Pac4jHandler extends DefaultSecurityLogic<Result, PlayWebContext> implements DeadboltHandler {

    private final Config config;

    private final HttpExecutionContext httpExecutionContext;

    private final String clients;

    private final PlaySessionStore playSessionStore;

    private final Pac4jRoleHandler rolePermissionsHandler;

    public Pac4jHandler(final Config config, final HttpExecutionContext httpExecutionContext, final String clients, final PlaySessionStore playSessionStore, final Pac4jRoleHandler rolePermissionsHandler) {
        CommonHelper.assertNotNull("config", config);
        CommonHelper.assertNotNull("httpExecutionContext", httpExecutionContext);
        CommonHelper.assertNotNull("playSessionStore", playSessionStore);

        this.config = config;
        this.httpExecutionContext = httpExecutionContext;
        this.clients = clients;
        this.playSessionStore = playSessionStore;
        this.rolePermissionsHandler = rolePermissionsHandler;
    }

    @Override
    public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.Context context) {
        return CompletableFuture.supplyAsync(() -> {
            final Optional<CommonProfile> profile = getProfile(context);
            if (profile.isPresent()) {
                logger.debug("profile found -> returning empty");
                return Optional.empty();
            } else {
                final PlayWebContext playWebContext = new PlayWebContext(context, playSessionStore);
                final HttpActionAdapter<Result, PlayWebContext> httpActionAdapter = config.getHttpActionAdapter();
                final List<Client> currentClients = getClientFinder().find(config.getClients(), playWebContext, clients);
                logger.debug("currentClients: {}", currentClients);

                HttpAction action;
                try {
                    if (startDirectAuthentication(currentClients)) {
                        logger.debug("Starting direct authentication");
                        DirectClient client = (DirectClient) currentClients.get(0);
                        Credentials credentials = client.getCredentials(playWebContext);
                        if (credentials != null) {
                            CommonProfile userProfile = credentials.getUserProfile();
                            if (userProfile != null) {
                                setProfile(context, userProfile);
                                return Optional.empty();
                            }
                        }
                        logger.debug("unauthorized");
                        action = unauthorized(playWebContext, currentClients);
                    } else if (startAuthentication(playWebContext, currentClients)) {
                        logger.debug("Starting authentication");
                        saveRequestedUrl(playWebContext, currentClients);
                        action = redirectToIdentityProvider(playWebContext, currentClients);
                    } else {
                        logger.debug("unauthorized");
                        action = unauthorized(playWebContext, currentClients);
                    }
                } catch (final HttpAction e) {
                    action = e;
                }
                return Optional.of(httpActionAdapter.adapt(action.getCode(), playWebContext));
            }
        }, httpExecutionContext.current());
    }

    @Override
    public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context) {
        return CompletableFuture.supplyAsync(() -> {
            final Optional<CommonProfile> profile = getProfile(context);
            if (profile.isPresent()) {
                logger.debug("profile found: {} -> building a subject", profile);
                return Optional.of(new Pac4jSubject(profile.get()));
            } else {
                logger.debug("no profile found -> returning empty");
                return Optional.empty();
            }
        }, httpExecutionContext.current());
    }

    @Override
    public CompletionStage<List<? extends Permission>> getPermissionsForRole(String roleName) {
        return rolePermissionsHandler.getPermissionsForRole(clients, roleName, httpExecutionContext);
    }

    private Optional<CommonProfile> getProfile(final Http.Context context) {
        final PlayWebContext playWebContext = new PlayWebContext(context, playSessionStore);
        final ProfileManager manager = new ProfileManager(playWebContext);
        return manager.get(true);
    }

    private void setProfile(final Http.Context context, CommonProfile profile) {
        final PlayWebContext playWebContext = new PlayWebContext(context, playSessionStore);
        playWebContext.setRequestAttribute(Pac4jConstants.USER_PROFILES, profile);
    }

    @Override
    public CompletionStage<Result> onAuthFailure(final Http.Context context, final Optional<String> content) {
        return CompletableFuture.supplyAsync(() -> {
            final PlayWebContext playWebContext = new PlayWebContext(context, playSessionStore);
            final HttpActionAdapter<Result, PlayWebContext> httpActionAdapter = config.getHttpActionAdapter();
            return httpActionAdapter.adapt(403, playWebContext);
        }, httpExecutionContext.current());
    }

    @Override
    public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.Context context) {
        throw new TechnicalException("getDynamicResourceHandler() not supported in Pac4jHandler");
    }

    private boolean startDirectAuthentication(final List<Client> currentClients) {
        return isNotEmpty(currentClients) && currentClients.get(0) instanceof DirectClient;
    }
}
