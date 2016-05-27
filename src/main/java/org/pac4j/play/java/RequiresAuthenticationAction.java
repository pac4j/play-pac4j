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
package org.pac4j.play.java;

import org.pac4j.core.authorization.AuthorizationChecker;
import org.pac4j.core.authorization.DefaultAuthorizationChecker;
import org.pac4j.core.client.*;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.play.PlayWebContext;

import play.libs.concurrent.HttpExecution;
import play.mvc.Http.Context;
import play.mvc.Result;

import javax.inject.Inject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * <p>This filter protects a resource (authentication + authorization).</p>
 * <ul>
 *  <li>If a stateful / indirect client is used, it relies on the session to get the user profile (after the {@link org.pac4j.play.CallbackController} has terminated the authentication process)</li>
 *  <li>If a stateless / direct client is used, it validates the provided credentials from the request and retrieves the user profile if the authentication succeeds.</li>
 * </ul>
 * <p>Then, authorizations are checked before accessing the resource.</p>
 * <p>Forbidden or unauthorized errors can be returned. An authentication process can be started (redirection to the identity provider) in case of an indirect client.</p>
 * <p>The configuration can be provided via annotation parameters: <code>clientName</code> and <code>authorizerName</code>.</p>
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @since 1.0.0
 */
public class RequiresAuthenticationAction extends AbstractConfigAction {

    @Inject
    protected Config config;

    protected ClientFinder clientFinder = new DefaultClientFinder();

    protected AuthorizationChecker authorizationChecker = new DefaultAuthorizationChecker();

    public RequiresAuthenticationAction() {
    }

    public RequiresAuthenticationAction(final Config config) {
        this.config = config;
    }

    @Override
    public CompletionStage<Result> call(final Context ctx){
      try{
        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(configuration);
        final String clientName = getStringParam(invocationHandler, CLIENT_NAME_METHOD, null);
        final String authorizerName = getStringParam(invocationHandler, AUTHORIZER_NAME_METHOD, null);

        return internalCall(ctx, clientName, authorizerName);
      }catch(Throwable t){
        throw new RuntimeException(t);
      }
    }

    public CompletionStage<Result> internalCall(final Context ctx, final String clientName, final String authorizerName) throws Throwable {

        final PlayWebContext context =  new PlayWebContext(ctx, config.getSessionStore());
        logger.debug("url: {}", context.getFullRequestURL());

        CommonHelper.assertNotNull("config", config);
        CommonHelper.assertNotNull("config.httpActionAdapter", config.getHttpActionAdapter());
        final Clients configClients = config.getClients();
        CommonHelper.assertNotNull("configClients", configClients);
        logger.debug("clientName: {}", clientName);
        final List<Client> currentClients = clientFinder.find(configClients, context, clientName);
        logger.debug("currentClients: {}", currentClients);

        final boolean useSession = useSession(context, currentClients);
        logger.debug("useSession: {}", useSession);

        final CompletionStage<UserProfile> promiseProfile = CompletableFuture.supplyAsync(() -> {

            final ProfileManager manager = new ProfileManager(context);
            UserProfile profile = manager.get(useSession);
            logger.debug("profile: {}", profile);

            // no profile and some current clients
            if (profile == null && currentClients != null && !currentClients.isEmpty()) {
                // loop on all clients searching direct ones to perform authentication
                for (final Client currentClient : currentClients) {
                    if (currentClient instanceof DirectClient) {
                        logger.debug("Performing authentication for client: {}", currentClient);
                        final Credentials credentials;
                        try {
                            credentials = currentClient.getCredentials(context);
                            logger.debug("credentials: {}", credentials);
                        } catch (final RequiresHttpAction e) {
                            throw new TechnicalException("Unexpected HTTP action", e);
                        }
                        profile = currentClient.getUserProfile(credentials, context);
                        logger.debug("profile: {}", profile);
                        if (profile != null) {
                            manager.save(useSession, profile);
                            break;
                        }
                    }
                }
            }

            return profile;
        });
        
        return promiseProfile.thenComposeAsync((UserProfile profile) ->{
          if (profile != null) {
              logger.debug("authorizerName: {}", authorizerName);
              if (authorizationChecker.isAuthorized(context, profile, authorizerName, config.getAuthorizers())) {
                  logger.debug("authenticated and authorized -> grant access");
                  // when called from Scala
                  if (delegate == null) {
                      return CompletableFuture.completedFuture(null);
                  } else {
                      return delegate.call(ctx);
                  }
              } else {
                  logger.debug("forbidden");
                  return forbidden(context, currentClients, profile);
              }
          } else {
              if (startAuthentication(context, currentClients)) {
                  logger.debug("Starting authentication");
                  saveRequestedUrl(context, currentClients);
                  return CompletableFuture.completedFuture(
                    redirectToIdentityProvider(context, currentClients));
              } else {
                  logger.debug("unauthorized");
                  return unauthorized(context, currentClients);
              }
          }
        }, HttpExecution.defaultContext());
    }

    protected boolean useSession(final WebContext context, final List<Client> currentClients) {
        return currentClients == null || currentClients.isEmpty() || currentClients.get(0) instanceof IndirectClient;
    }

    protected CompletionStage<Result> forbidden(final PlayWebContext context, final List<Client> currentClients, final UserProfile profile) {
        return CompletableFuture.completedFuture((Result) config.getHttpActionAdapter().adapt(HttpConstants.FORBIDDEN, context));
    }

    protected boolean startAuthentication(final PlayWebContext context, final List<Client> currentClients) {
        return currentClients != null && !currentClients.isEmpty() && currentClients.get(0) instanceof IndirectClient;
    }

    protected void saveRequestedUrl(final WebContext context, final List<Client> currentClients) {
        final String requestedUrl = context.getFullRequestURL();
        logger.debug("requestedUrl: {}", requestedUrl);
        context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
    }

    protected Result redirectToIdentityProvider(final PlayWebContext context, final List<Client> currentClients) {
        try {
            final IndirectClient currentClient = (IndirectClient) currentClients.get(0);
            currentClient.redirect(context, true);
            return (Result) config.getHttpActionAdapter().adapt(context.getResponseStatus(), context);
        } catch (final RequiresHttpAction e) {
            logger.debug("extra HTTP action required: {}", e.getCode());
            return (Result) config.getHttpActionAdapter().adapt(e.getCode(), context);
        }
    }

    protected CompletionStage<Result> unauthorized(final PlayWebContext context, final List<Client> currentClients) {
        return CompletableFuture.completedFuture((Result) config.getHttpActionAdapter().adapt(HttpConstants.UNAUTHORIZED, context));
    }
}
