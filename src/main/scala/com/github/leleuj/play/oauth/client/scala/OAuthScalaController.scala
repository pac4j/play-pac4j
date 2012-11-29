/*
  Copyright 2012 Jerome Leleu

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
package com.github.leleuj.play.oauth.client.scala

import play.api._
import play.api.mvc._
import org.scribe.up.profile._
import org.slf4j._
import play.api.cache.Cache
import com.github.leleuj.play.oauth.client._
import org.apache.commons.lang3.StringUtils
import play.api.Play.current

/**
 * This controller is the Scala controller to retrieve the user profile or the redirect url to start the OAuth authentication process.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
 trait OAuthScalaController extends Controller {

  protected val logger = LoggerFactory.getLogger("com.github.leleuj.play.oauth.client.scala.OAuthScalaController")

  /**
   * Defines an action with requires OAuth authentication : it means that the user is redirected to the OAuth provider
   * if he is not "OAuth authenticated" or access directly to the action otherwise.
   * 
   * @param providerType
   * @param targetUrl
   * @param action
   * @return the current action to process or the redirection to the OAuth provider if the user is not "OAuth authenticated"
   */
  protected def RequiresOAuthAuthentication(providerType: String, targetUrl: String = "")(action: UserProfile => Action[AnyContent]) = Action { request =>
    val userProfile = profile(request)
    if (userProfile == null) {
      val (url, newSession) = redirectUrl(request, providerType, targetUrl)
      Redirect(url).withSession(newSession)
    } else { 
      action(userProfile)(request)
    }
  }

  /**
   * Retrieves the OAuth user profile if the user is "OAuth authenticated" or <code>null</code> otherwise.
   * 
   * @return the current action to process
   */
  protected def OAuthProfile(action: UserProfile => Action[AnyContent]) = Action { request =>
    val userProfile = profile(request)
    action(userProfile)(request)
  }

  /**
   * Returns the redirection url to the OAuth provider for authentication.
   *
   * @param request
   * @param providerType
   * @param targetUrl 
   * @return (the redirection url to the OAuth provider, the updated session)
   */
  protected def redirectUrl(request: RequestHeader, providerType: String, targetUrl: String = ""): (String, Session) = {
    // save requested url to session
    val savedRequestUrl = OAuthController.getRedirectUrl(targetUrl, request.uri)
    logger.debug("save url before redirectUrl : {}", savedRequestUrl)
    val scalaUserSession = new ScalaUserSession(request.session + (OAuthConstants.OAUTH_REQUESTED_URL -> savedRequestUrl))
    // redirect to the OAuth provider for authentication
    val redirectUrl = OAuthConfiguration.getProvidersDefinition().findProvider(providerType).getAuthorizationUrl(scalaUserSession)
    logger.debug("redirectUrl to : {}", redirectUrl)
    (redirectUrl, scalaUserSession.getSession)
  }

  /**
   * Returns the OAuth profile.
   *
   * @param request
   * @return the OAuth profile
   */
  private def profile(request: RequestHeader): UserProfile = {
    // get the session id
    var profile: UserProfile = null
    val sessionId = request.session.get(OAuthConstants.OAUTH_SESSION_ID)
    logger.debug("profile for sessionId : {}", sessionId)
    if (sessionId.isDefined && StringUtils.isNotBlank(sessionId.get)) {
      // get the user profile in cache
      val userProfile: Option[UserProfile] = Cache.getAs[UserProfile](sessionId.get)
      if (userProfile.isDefined) {
        logger.debug("userProfile : {}", userProfile.get)
        profile = userProfile.get
      }
    }
    profile
  }

}
