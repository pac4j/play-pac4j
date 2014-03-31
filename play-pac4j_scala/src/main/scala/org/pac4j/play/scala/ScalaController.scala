/*
  Copyright 2012 - 2014 Jerome Leleu

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

import scala.concurrent.Future

import play.api._
import play.api.mvc._
import org.pac4j.core.client._
import org.pac4j.core.credentials._
import org.pac4j.core.profile._
import org.pac4j.core.util._
import org.pac4j.play._
import org.slf4j._
import play.core.server.netty.RequestBodyHandler
import org.pac4j.core.exception._

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
   * @param parser
   * @param isAjax
   * @param action
   * @return the current action to process or the redirection to the provider if the user is not authenticated
   */
  protected def RequiresAuthentication[A](clientName: String, targetUrl: String, parser:BodyParser[A], isAjax: Boolean = false)(action: CommonProfile => Action[A]) = Action.async(parser) { request =>
    logger.debug("Entering RequiresAuthentication")
    var newSession = getOrCreateSessionId(request)
    val sessionId = newSession.get(Constants.SESSION_ID).get
    logger.debug("sessionId : {}", sessionId)
    val profile = getUserProfile(request)
    logger.debug("profile : {}", profile)
   
    if (profile == null) {
      try {
        val redirectAction = getRedirectAction(request, newSession, clientName, targetUrl, true, isAjax)
        logger.debug("redirectAction : {}", redirectAction)          
        if (redirectAction.getType() == RedirectAction.RedirectType.REDIRECT) Future.successful(Redirect(redirectAction.getLocation()).withSession(newSession))
        else Future.successful(Ok(redirectAction.getContent()).withSession(newSession))
      } catch {
        case ex: RequiresHttpAction => {
          val code = ex.getCode()
          if (code == 401) {
            Future.successful(Unauthorized(Config.getErrorPage401()).as(HTML))
          } else if (code == 403) {
            Future.successful(Forbidden(Config.getErrorPage403()).as(HTML))
          } else {
            throw new TechnicalException("Unexpected HTTP code : " + code)
          }
        }
      }
    } else {
      action(profile)(request)      
    }
  }
  
  protected def RequiresAuthentication(clientName: String, targetUrl: String = "", isAjax: Boolean = false)(action: CommonProfile => Action[AnyContent]): Action[AnyContent] = { 
    RequiresAuthentication(clientName, targetUrl, parse.anyContent, isAjax)(action)
  }

    /**
   * Returns the redirection action to the provider for authentication.
   *
   * @param request
   * @param newSession
   * @param clientName
   * @param targetUrl
   * @return the redirection url to the provider
   */
  protected def getRedirectAction[A](request: Request[A], newSession: Session, clientName: String, targetUrl: String = ""): RedirectAction = {
    var action:RedirectAction = null
    try {
      // redirect to the provider for authentication
      action = getRedirectAction(request, newSession, clientName, targetUrl, false, false)
    } catch {
      case ex: RequiresHttpAction => {
        // should not happen
      }
    }
    logger.debug("redirectAction to : {}", action)
    action    
  }

  /**
   * Returns the redirection action to the provider for authentication.
   *
   * @param request
   * @param newSession
   * @param clientName
   * @param targetUrl
   * @param protectedPage
   * @param isAjax
   * @return the redirection url to the provider
   */
  private def getRedirectAction[A](request: Request[A], newSession: Session, clientName: String, targetUrl: String, protectedPage: Boolean, isAjax: Boolean): RedirectAction = {
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
    val client = clients.findClient(clientName) match { case c:BaseClient[_,_] => c }
    val action = client.getRedirectAction(scalaWebContext, protectedPage, isAjax)
    logger.debug("redirectAction to : {}", action)
    action
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
