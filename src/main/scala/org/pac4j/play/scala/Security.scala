package org.pac4j.play.scala

import javax.inject.Inject

import org.pac4j.core.config.Config
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.play.PlayWebContext
import org.pac4j.play.java.SecureAction
import org.pac4j.play.store.PlaySessionStore
import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.core.j.JavaHelpers
import play.libs.concurrent.HttpExecutionContext
import play.mvc.Http.RequestBody

import scala.collection.JavaConverters
import scala.concurrent.Future
import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._

/**
 * <p>To protect a resource, the {@link #Secure} methods must be used.</p>
 * <p>For manual computation of login urls (redirections to identity providers), the session must be first initialized using the {@link #getOrCreateSessionId} method.</p>
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @author Hugo Valk
 * @since 1.5.0
 */
trait Security[P<:CommonProfile] extends Controller {

  protected val logger = LoggerFactory.getLogger(getClass)

  @Inject
  protected val config: Config
  @Inject
  protected val playSessionStore: PlaySessionStore
  @Inject
  protected val ec: HttpExecutionContext = null

  /**
   * Get or create a new sessionId.
   *
   * @param request
   * @return the (updated) session
   */
  protected def getOrCreateSessionId(request: RequestHeader): Session = {
    val webContext = new PlayWebContext(request, playSessionStore)
    webContext.getSessionStore.asInstanceOf[PlaySessionStore].getOrCreateSessionId(webContext)
    val map = JavaConverters.mapAsScalaMapConverter(webContext.getJavaSession).asScala.toMap
    new Session(map)
  }

  protected def Secure[A](action: List[P] => Action[AnyContent]): Action[AnyContent] = {
    Secure(null, null)(action)
  }

  protected def Secure[A](clients: String)(action: List[P] => Action[AnyContent]): Action[AnyContent] = {
    Secure(clients, null)(action)
  }

  protected def Secure[A](clients: String, authorizers: String, multiProfile: Boolean = false)(action: List[P] => Action[AnyContent]): Action[AnyContent] = {
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
  protected def Secure[A](parser: BodyParser[A], clients: String, authorizers: String, multiProfile: Boolean)(action: List[P] => Action[A]) = Action.async(parser) { request =>
    val webContext = request.body match {
      case content: AnyContentAsFormUrlEncoded =>
        val javaBodyContent = content.asFormUrlEncoded
          .getOrElse(Map.empty[String, Seq[String]])
          .map(field => field._1 -> field._2.toArray)  // Make field values Java-friendly
          .asJava
        val javaBody = new RequestBody(javaBodyContent)
        val jRequest = Request(request, javaBody)
        val jContext = JavaHelpers.createJavaContext(jRequest, JavaHelpers.createContextComponents())
        new PlayWebContext(jContext, playSessionStore)
      case _ =>
        new PlayWebContext(request, playSessionStore)
    }

    val secureAction = new SecureAction(config, playSessionStore, ec)
    val javaContext = webContext.getJavaContext
    secureAction.internalCall(javaContext, clients, authorizers, multiProfile).toScala.flatMap[play.api.mvc.Result](r =>
      if (r == null) {
        val profileManager = new ProfileManager[P](webContext)
        val profiles = profileManager.getAll(true)
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
