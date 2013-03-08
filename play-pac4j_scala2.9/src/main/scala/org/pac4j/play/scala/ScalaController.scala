/*
  Copyright 2012 - 2013 Jerome Leleu

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
package org.pac4j.play.scala

import play.api._
import play.api.mvc._
import org.pac4j.core.client._
import org.pac4j.core.credentials._
import org.pac4j.core.profile._
import org.pac4j.core.util._
import org.pac4j.play._
import org.slf4j._
import play.core.server.netty.RequestBodyHandler
import org.pac4j.core.exception.TechnicalException

/**
 * This controller is the Scala controller to retrieve the user profile or the redirection url to start the authentication process.
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
trait ScalaController extends Controller {

  protected val logger = LoggerFactory.getLogger("org.pac4j.play.scala.ScalaController")

  /**
   * Get or create a new sessionId.
   *
   * @param request
   * @return the (updated) session
   */
  protected def getOrCreateSessionId(request: RequestHeader): Session = {
    var sessionId: String = null
    var newSession = request.session
    val optionSessionId = newSession.get(Constants.SESSION_ID)
    logger.debug("getOrCreateSessionId : {}", optionSessionId)
    if (!optionSessionId.isDefined) {
      newSession += Constants.SESSION_ID -> StorageHelper.generateSessionId()
    }
    newSession
  }

  /**
   * Defines an action with requires authentication : it means that the user is redirected to the provider
   * if he is not authenticated or access directly to the action otherwise.
   *
   * @param clientName
   * @param targetUrl
   * @param action
   * @return the current action to process or the redirection to the provider if the user is not authenticated
   */
  protected def RequiresAuthentication(clientName: String, targetUrl: String = "")(action: CommonProfile => Action[AnyContent]) = Action { request =>
    var newSession = getOrCreateSessionId(request)
    val sessionId = newSession.get(Constants.SESSION_ID).get
    logger.debug("sessionId : {}", sessionId)
    val profile = getUserProfile(request)
    logger.debug("profile : {}", profile)
    if (profile == null) {
      val startAuth = StorageHelper.get(sessionId, clientName + Constants.START_AUTHENTICATION_SUFFIX).asInstanceOf[String]
      logger.debug("startAuth : {}", startAuth);
      StorageHelper.remove(sessionId, clientName + Constants.START_AUTHENTICATION_SUFFIX)
      if (CommonHelper.isNotBlank(startAuth)) {
        logger.error("not authenticated successfully to access a protected area -> forbidden")
        Forbidden(Config.getErrorPage403()).as(HTML)
      } else {
        val redirectionUrl = getRedirectionUrl(request, newSession, clientName, targetUrl, true)
        logger.debug("redirectionUrl : {}", redirectionUrl)
        StorageHelper.save(sessionId, clientName + Constants.START_AUTHENTICATION_SUFFIX, "true")
        Redirect(redirectionUrl).withSession(newSession)
      }
    } else {
      action(profile)(request)
    }
  }

  /**
   * Returns the redirection url to the provider for authentication.
   *
   * @param request
   * @param newSession
   * @param clientName
   * @param targetUrl
   * @param forceDirectRedirection
   * @return the redirection url to the provider
   */
  protected def getRedirectionUrl(request: Request[AnyContent], newSession: Session, clientName: String, targetUrl: String = "", forceDirectRedirection: Boolean = false): String = {
    val sessionId = newSession.get(Constants.SESSION_ID).get
    logger.debug("sessionId for getRedirectionUrl() : {}", sessionId)
    // save requested url to save
    val requestedUrlToSave = CallbackController.defaultUrl(targetUrl, request.uri)
    logger.debug("requestedUrlToSave : {}", requestedUrlToSave)
    StorageHelper.saveRequestedUrl(sessionId, clientName, requestedUrlToSave);
    // context
    val scalaWebContext = new ScalaWebContext(request, newSession)
    // clients
    val clients = Config.getClients()
    if (clients == null) {
      throw new TechnicalException("No client defined. Use Config.setClients(clients)")
    }
    // redirect to the provider for authentication
    val redirectionUrl = clients.findClient(clientName).asInstanceOf[BaseClient[Credentials, CommonProfile]].getRedirectionUrl(scalaWebContext, forceDirectRedirection)
    logger.debug("redirectionUrl to : {}", redirectionUrl)
    redirectionUrl
  }

  /**
   * Returns the user profile.
   *
   * @param request
   * @return the user profile
   */
  protected def getUserProfile(request: RequestHeader): CommonProfile = {
    // get the session id
    var profile: CommonProfile = null
    val sessionId = request.session.get(Constants.SESSION_ID)
    logger.debug("sessionId for profile : {}", sessionId)
    if (sessionId.isDefined) {
      // get the user profile
      profile = StorageHelper.getProfile(sessionId.get)
      logger.debug("profile : {}", profile)
    }
    profile
  }
}
