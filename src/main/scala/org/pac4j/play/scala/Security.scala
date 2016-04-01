package org.pac4j.play.scala

import javax.inject.Inject

import org.pac4j.core.config.Config
import org.pac4j.core.context.Pac4jConstants
import org.pac4j.core.profile.{UserProfile, ProfileManager}
import org.pac4j.core.util.CommonHelper
import org.pac4j.play.PlayWebContext
import org.pac4j.play.java.SecurityAction
import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.core.j.JavaHelpers

import _root_.scala.collection.JavaConverters
import _root_.scala.concurrent.Future
import collection.JavaConversions._

/**
 * <p>To protect a resource, the {@link #Secure} methods must be used.</p>
 * <p>For manual computation of login urls (redirections to identity providers), the session must be first initialized using the {@link #getOrCreateSessionId} method.</p>
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @author Hugo Valk
 * @since 1.5.0
 */
trait Security extends Controller {

  protected val logger = LoggerFactory.getLogger(getClass)

  @Inject
  protected var config: Config = null

  /**
   * Get or create a new sessionId.
   *
   * @param request
   * @return the (updated) session
   */
  protected def getOrCreateSessionId(request: RequestHeader): Session = {
    val webContext = new PlayWebContext(request, config.getSessionStore)
    webContext.getSessionStore.getOrCreateSessionId(webContext)
    val map = JavaConverters.mapAsScalaMapConverter(webContext.getJavaSession).asScala.toMap
    new Session(map)
  }

  protected def Secure[A](action: List[UserProfile] => Action[AnyContent]): Action[AnyContent] = {
    Secure(null, null)(action)
  }

  protected def Secure[A](clients: String)(action: List[UserProfile] => Action[AnyContent]): Action[AnyContent] = {
    Secure(clients, null)(action)
  }

  protected def Secure[A](clients: String, authorizers: String, multiProfile: Boolean = false)(action: List[UserProfile] => Action[AnyContent]): Action[AnyContent] = {
    Secure(parse.anyContent, clients, authorizers, multiProfile)(action)
  }

  /**
   * This function is used to protect an action.
   *
   * @param parser
   * @param clients the list of clients (separated by commas) to use for authentication
   * @param authorizers the list of authorizers (separated by commas) to use to check authorizations
   * @param multiProfile whether multiple authentications (and thus multiple profiles) must be kept at the same time
   * @param action
   * @tparam A
   * @return
   */
  protected def Secure[A](parser: BodyParser[A], clients: String, authorizers: String, multiProfile: Boolean)(action: List[UserProfile] => Action[A]) = Action.async(parser) { request =>
    val webContext = new PlayWebContext(request, config.getSessionStore)
    val securityAction = new SecurityAction(config)
    val javaContext = webContext.getJavaContext
    securityAction.internalCall(javaContext, clients, authorizers, multiProfile).wrapped().flatMap[play.api.mvc.Result](r =>
      if (r == null) {
        var profiles = javaContext.args.get(Pac4jConstants.USER_PROFILES).asInstanceOf[java.util.List[UserProfile]]
        if (CommonHelper.isEmpty(profiles)) {
          val profileManager = new ProfileManager(webContext)
          profiles = profileManager.getAll(true)
        }
        logger.debug("profiles: {}", profiles)
        action(asScalaBuffer(profiles).toList)(request)
      } else {
        Future {
          JavaHelpers.createResult(javaContext, r)
        }
      }
    )
  }
}
