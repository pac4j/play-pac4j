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
package org.pac4j.play.scala

import javax.inject.Inject

import org.pac4j.core.config.Config
import org.pac4j.core.context.Pac4jConstants
import org.pac4j.core.profile._
import org.pac4j.play.http.HttpActionAdapter
import org.pac4j.play.java.RequiresAuthenticationAction
import org.pac4j.play.store.DataStore
import play.api.mvc._
import play.core.j.JavaHelpers

import scala.collection.JavaConverters
import _root_.scala.concurrent.Future
import org.pac4j.play._
import org.slf4j._

import play.api.libs.concurrent.Execution.Implicits._

/**
 * <p>This trait adds security features to your Scala controllers.</p>
 * <p>For manual computation of login urls (redirections to identity providers), the session must be first initialized using the {@link #getOrCreateSessionId} method.</p>
 * <p>To protect a resource, the {@link #RequiresAuthentication} methods must be used.</p>
 * <p>To get the current user profile, the {@link #getUserProfile} method must be called.</p>
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @author Hugo Valk
 * @since 1.5.0
 */
trait Security[P<:CommonProfile] extends Controller {

  protected val logger = LoggerFactory.getLogger(getClass)

  @Inject
  protected var config: Config = null

  @Inject
  protected var dataStore: DataStore = null

  @Inject
  protected var httpActionHandler: HttpActionAdapter = null

  /**
   * Get or create a new sessionId.
   *
   * @param request
   * @return the (updated) session
   */
  protected def getOrCreateSessionId(request: RequestHeader): Session = {
    val webContext = new PlayWebContext(request, dataStore)
    dataStore.getOrCreateSessionId(webContext)
    val map = JavaConverters.mapAsScalaMapConverter(webContext.getJavaSession).asScala.toMap
    new Session(map)
  }

  protected def RequiresAuthentication[A](action: P => Action[AnyContent]): Action[AnyContent] = {
    RequiresAuthentication(null, null)(action)
  }

  protected def RequiresAuthentication[A](clientName: String)(action: P => Action[AnyContent]): Action[AnyContent] = {
    RequiresAuthentication(clientName, null)(action)
  }

  protected def RequiresAuthentication[A](clientName: String, authorizerName: String)(action: P => Action[AnyContent]): Action[AnyContent] = {
    RequiresAuthentication(parse.anyContent, clientName, authorizerName)(action)
  }

  /**
   * This function is used to protect a resource.
   *
   * @param parser
   * @param clientName the list of clients (separated by commas) to use for authentication
   * @param authorizerName the list of authorizers (separated by commas) to use to check authorizations
   * @param action
   * @tparam A
   * @return
   */
  protected def RequiresAuthentication[A](parser: BodyParser[A], clientName: String, authorizerName: String)(action: P => Action[A]) = Action.async(parser) { request =>
    val webContext = new PlayWebContext(request, dataStore)
    val requiresAuthenticationAction = new RequiresAuthenticationAction(config, dataStore, httpActionHandler)
    val javaContext = webContext.getJavaContext
    requiresAuthenticationAction.internalCall(javaContext, clientName, authorizerName).wrapped().flatMap[play.api.mvc.Result](r =>
      if (r == null) {
        var profile = javaContext.args.get(Pac4jConstants.USER_PROFILE).asInstanceOf[P]
        if (profile == null) {
          profile = getUserProfile(request)
        }
        action(profile)(request)
      } else {
        Future {
          JavaHelpers.createResult(javaContext, r)
        }
      }
    )
  }

  /**
   * Return the current user profile.
   *
   * @param request
   * @return the user profile
   */
  protected def getUserProfile(request: RequestHeader): P = {
    val webContext = new PlayWebContext(request, dataStore)
    val profileManager = new ProfileManager[P](webContext)
    profileManager.get(true)
  }
}
