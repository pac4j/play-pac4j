package org.pac4j.play.scala

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import play.api.mvc._
import org.pac4j.core.config.Config
import org.pac4j.core.profile.UserProfile
import org.pac4j.play.context.PlayFrameworkParameters
import org.pac4j.play.result.PlayWebContextResultHolder
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}

/**
 * <p>To protect a resource, the {@link #Secure} methods must be used.</p>
 * <p>For manual computation of login urls (redirections to identity providers), the session must be first initialized
 * using the {@link #getSessionId} method with <code>createSession</code> set to <code>true</code>.</p>
 *
 * @author Jerome Leleu
 * @author Michael Remond
 * @author Hugo Valk
 * @since 1.5.0
 */
trait Security[P<:UserProfile] extends BaseController {

  protected type AuthenticatedRequest[A] = org.pac4j.play.scala.AuthenticatedRequest[P, A]

  protected def controllerComponents: SecurityComponents

  protected def config: Config = controllerComponents.config

  protected def profiles[A](implicit request: AuthenticatedRequest[A]): List[P] = request.profiles

  protected def Secure: SecureAction[P,AnyContent,AuthenticatedRequest] =
    SecureAction[P,AnyContent,AuthenticatedRequest](clients = null, authorizers = null, matchers = null, controllerComponents.parser, config)(controllerComponents.executionContext)
}


case class SecureAction[P <: UserProfile, ContentType, R[X]>:AuthenticatedRequest[P, X]<:Request[X]](
  clients: String, authorizers: String, matchers: String, parser: BodyParser[ContentType], config: Config
)(implicit implicitExecutionContext: ExecutionContext) extends ActionBuilder[R, ContentType] {
  import ScalaCompat.Converters._
  import scala.compat.java8.FutureConverters._
  import scala.concurrent.Future
  import org.pac4j.play.scala.SecureAction._

  protected def executionContext: ExecutionContext = implicitExecutionContext


  /**
    * This function is used to protect an action.
    *
    * @param clients the list of clients (separated by commas) to use for authentication
    * @param authorizers the list of authorizers (separated by commas) to use to check authorizations
    * @param matchers the list of matchers (separated by commas)
    * @return
    */
  def apply(clients: String = clients, authorizers: String = authorizers, matchers: String = matchers): SecureAction[P,ContentType,R] =
    copy[P,ContentType,R](clients, authorizers, matchers)

  /**
    * This function is used to protect an action.
    *
    * @param action nested action assuming authenticated and authorized users
    * @tparam A content type
    * @return
    */
  def apply[A](action: Action[A]): Action[A] =
    copy[P,A,R](parser = action.parser).async(action.parser)(r => action.apply(r))

  def invokeBlock[A](request: Request[A], block: R[A] => Future[Result]): Future[Result] = {
    val secureAction = new org.pac4j.play.java.SecureAction(config)
    val parameters = new PlayFrameworkParameters(request)
    secureAction.call(parameters, clients, authorizers, matchers).toScala.flatMap[play.api.mvc.Result] {
      case holder: PlayWebContextResultHolder =>
        val webContext = holder.getPlayWebContext
        val sessionStore = config.getSessionStoreFactory.newSessionStore(parameters)
        val profileManager = config.getProfileManagerFactory.apply(webContext, sessionStore)
        val profiles = profileManager.getProfiles
        logger.debug("profiles: {}", profiles)
        val sProfiles = profiles.asScala.toList.asInstanceOf[List[P]]
        val sRequest = webContext.supplementRequest(request)
        block(AuthenticatedRequest(sProfiles, sRequest))
      case r =>
        Future successful {
          r.asScala
        }
    }
  }
}

object SecureAction {
  private val logger = org.slf4j.LoggerFactory.getLogger(getClass)
}

case class AuthenticatedRequest[P<:UserProfile, +A](profiles: List[P], request: Request[A]) extends WrappedRequest[A](request)

trait SecurityComponents extends ControllerComponents {

  def components: ControllerComponents
  def config: Config
  def parser: BodyParsers.Default

  @inline def actionBuilder: ActionBuilder[Request, AnyContent] = components.actionBuilder
  @inline def parsers: PlayBodyParsers = components.parsers
  @inline def messagesApi: MessagesApi = components.messagesApi
  @inline def langs: Langs = components.langs
  @inline def fileMimeTypes: FileMimeTypes = components.fileMimeTypes
  @inline def executionContext: ExecutionContext = components.executionContext
}

@Singleton
case class DefaultSecurityComponents @Inject()
(
  config: Config,
  parser: BodyParsers.Default,
  components: ControllerComponents
) extends SecurityComponents
