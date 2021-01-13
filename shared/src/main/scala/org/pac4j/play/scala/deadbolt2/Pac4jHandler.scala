package org.pac4j.play.scala.deadbolt2

import java.util.Optional

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler}
import be.objectify.deadbolt.scala.models.Subject
import org.pac4j.core.client.{Client, DirectClient}
import org.pac4j.core.config.Config
import org.pac4j.core.context.HttpConstants
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.util.Pac4jConstants
import org.pac4j.core.engine.DefaultSecurityLogic
import org.pac4j.core.exception.TechnicalException
import org.pac4j.core.exception.http.{HttpAction, StatusAction}
import org.pac4j.core.profile.{ProfileManager, UserProfile}
import org.pac4j.core.util.CommonHelper.isNotEmpty
import org.pac4j.play.PlayWebContext
import play.api.Logger
import play.api.mvc.{Request, RequestHeader, Result}

/**
  * @author Zenkie Zhu
  * @since 4.1.0
  */
class Pac4jHandler(config: Config, clients: String, sessionStore: SessionStore, rolePermissionsHandler: Pac4jRoleHandler)(implicit ec: ExecutionContext)
    extends DefaultSecurityLogic with DeadboltHandler {

  private val logger = Logger(this.getClass)

  implicit def asScalaOption[B](o: Optional[B]) = if (o.isPresent) Some(o.get) else None

  override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = Future {
    val profile = getProfile(request)
    if (profile.isDefined) {
      logger.debug("profile found -> returning None")
      None
    } else {
      val playWebContext = new PlayWebContext(request)
      val currentClients = getClientFinder().find(config.getClients(), playWebContext, clients)

      logger.debug("currentClients: " + currentClients)

      val action = try {
        if (startDirectAuthentication(currentClients)) {
          logger.debug("Starting direct authentication")
          val client = currentClients.get(0).asInstanceOf[DirectClient]
          val credentials = client.getCredentials(playWebContext, sessionStore)
          if (credentials.isPresent()) {
            val userProfile = credentials.get().getUserProfile
            if (userProfile != null) {
              setProfile(request, userProfile)
              return Future { None }
            }
          }
          logger.debug("unauthorized")
          unauthorized(playWebContext, sessionStore, currentClients)
        } else if (startAuthentication(playWebContext, sessionStore, currentClients)) {
          logger.debug("Starting authentication")
          saveRequestedUrl(playWebContext, sessionStore, currentClients, null)
          redirectToIdentityProvider(playWebContext, sessionStore, currentClients)
        } else {
          logger.debug("unauthorized")
          unauthorized(playWebContext, sessionStore, currentClients)
        }
      } catch {
        case e: HttpAction => e
      }

      Some(httpActionAdapter.adapt(action, playWebContext).asInstanceOf[play.mvc.Result].asScala())
    }
  }

  override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] = Future {
    getProfile(request).map(Pac4jSubject(_))
  }

  override def getPermissionsForRole(roleName: String): Future[List[String]] =
    rolePermissionsHandler.getPermissionsForRole(clients, roleName)

  private def getProfile(request: RequestHeader): Option[UserProfile] = {
    val playWebContext = new PlayWebContext(request)
    val profileManager = new ProfileManager(playWebContext, sessionStore)
    profileManager.getProfile()
  }

  private def setProfile(request: RequestHeader, profile: UserProfile): Unit = {
    val playWebContext = new PlayWebContext(request)
    playWebContext.setRequestAttribute(Pac4jConstants.USER_PROFILES, profile)
  }

  override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] = Future {
    val playWebContext = new PlayWebContext(request)
    httpActionAdapter.adapt(new StatusAction(HttpConstants.FORBIDDEN), playWebContext).asInstanceOf[play.mvc.Result].asScala()
  }

  private def httpActionAdapter = config.getHttpActionAdapter()

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] =
    throw new TechnicalException("getDynamicResourceHandler() not supported in Pac4jHandler")

  private def startDirectAuthentication(currentClients: java.util.List[Client]): Boolean =
    isNotEmpty(currentClients) && currentClients.get(0).isInstanceOf[DirectClient]
}
