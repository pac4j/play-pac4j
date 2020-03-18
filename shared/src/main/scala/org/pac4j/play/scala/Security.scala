package org.pac4j.play.scala

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.language.higherKinds
import play.api.mvc._
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore

/**
 * <p>To protect a resource, the {@link #Secure} methods must be used.</p>
 * <p>For manual computation of login urls (redirections to identity providers), the session must be first initialized using the {@link #getOrCreateSessionId} method.</p>
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @author Hugo Valk
 * @since 1.5.0
 */
trait Security[P<:CommonProfile] extends BaseController {

  protected type AuthenticatedRequest[A] = org.pac4j.play.scala.AuthenticatedRequest[P, A]

  protected def controllerComponents: SecurityComponents

  protected def config: Config = controllerComponents.config

  protected def playSessionStore: PlaySessionStore = controllerComponents.playSessionStore

  protected def profiles[A](implicit request: AuthenticatedRequest[A]): List[P] = request.profiles

  protected def Secure: SecureAction[P,AnyContent,AuthenticatedRequest] =
    SecureAction[P,AnyContent,AuthenticatedRequest](clients = null, authorizers = null, matchers = null, multiProfile = false, controllerComponents.parser, playSessionStore, config)(controllerComponents.executionContext)
}


case class SecureAction[P<:CommonProfile, ContentType, R[X]>:AuthenticatedRequest[P, X]<:Request[X]](clients: String, authorizers: String, matchers: String, multiProfile: Boolean, parser: BodyParser[ContentType], playSessionStore: PlaySessionStore, config: Config)(implicit implicitExecutionContext: ExecutionContext) extends ActionBuilder[R, ContentType] {
  import scala.collection.JavaConverters._
  import scala.compat.java8.FutureConverters._
  import scala.concurrent.Future
  import org.pac4j.core.profile.ProfileManager
  import org.pac4j.play.scala.SecureAction._

  protected def executionContext = implicitExecutionContext


  /**
    * This function is used to protect an action.
    *
    * @param clients the list of clients (separated by commas) to use for authentication
    * @param authorizers the list of authorizers (separated by commas) to use to check authorizations
    * @param multiProfile whether multiple authentications (and thus multiple profiles) must be kept at the same time
    * @return
    */
  def apply(clients: String = clients, authorizers: String = authorizers, matchers: String = matchers, multiProfile: Boolean = multiProfile): SecureAction[P,ContentType,R] =
    copy[P,ContentType,R](clients, authorizers, matchers, multiProfile)

  /**
    * This function is used to protect an action.
    *
    * @param action nested action assuming authenticated and authorized users
    * @tparam A content type
    * @return
    */
  def apply[A](action: Action[A]): Action[A] =
    copy[P,A,R](parser = action.parser).async(action.parser)(r => action.apply(r))

  def invokeBlock[A](request: Request[A], block: R[A] => Future[Result]) = {
    val webContext = new PlayWebContext(request, playSessionStore)
    val secureAction = new org.pac4j.play.java.SecureAction(config, playSessionStore)
    secureAction.call(webContext, clients, authorizers, matchers, multiProfile).toScala.flatMap[play.api.mvc.Result](r =>
      if (r == null) {
        val profileManager = new ProfileManager[P](webContext)
        val profiles = profileManager.getAllLikeDefaultSecurityLogic(true)
        logger.debug("profiles: {}", profiles)
        block(AuthenticatedRequest(profiles.asScala.toList, webContext.supplementRequest(request.asJava).asScala.asInstanceOf[Request[A]]))
      } else {
        Future successful {
          r.asScala
        }
      }
    )
  }
}

object SecureAction {
  private val logger = org.slf4j.LoggerFactory.getLogger(getClass)
}

case class AuthenticatedRequest[P<:CommonProfile, +A](profiles: List[P], request: Request[A]) extends WrappedRequest[A](request)

trait SecurityComponents extends ControllerComponents {

  def components: ControllerComponents
  def config: Config
  def playSessionStore: PlaySessionStore
  def parser: BodyParsers.Default

  @inline def actionBuilder = components.actionBuilder
  @inline def parsers = components.parsers
  @inline def messagesApi = components.messagesApi
  @inline def langs = components.langs
  @inline def fileMimeTypes = components.fileMimeTypes
  @inline def executionContext = components.executionContext
}

@Singleton
case class DefaultSecurityComponents @Inject()
(
  playSessionStore: PlaySessionStore,
  config: Config,
  parser: BodyParsers.Default,
  components: ControllerComponents
) extends SecurityComponents
