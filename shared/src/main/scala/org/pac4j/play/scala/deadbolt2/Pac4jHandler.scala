package org.pac4j.play.scala.deadbolt2

import java.util.Optional

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler}
import be.objectify.deadbolt.scala.models.Subject
import org.pac4j.core.client.{Client, DirectClient}
import org.pac4j.core.config.Config
import org.pac4j.core.context.Pac4jConstants
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.engine.DefaultSecurityLogic
import org.pac4j.core.exception.{HttpAction, TechnicalException}
import org.pac4j.core.http.adapter.HttpActionAdapter
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.core.util.CommonHelper.isNotEmpty
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc.{Request, RequestHeader, Result}

/**
  * @author Zenkie Zhu
  * @since 4.1.0
  */
class Pac4jHandler(config: Config, clients: String, playSessionStore: PlaySessionStore, rolePermissionsHandler: Pac4jRoleHandler)(implicit ec: ExecutionContext)
    extends DefaultSecurityLogic[Result, PlayWebContext] with DeadboltHandler {

  implicit def asScalaOption[B](o: Optional[B]) = if (o.isPresent) Some(o.get) else None

  override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = Future {
    val profile = getProfile(request)
    if (profile.isDefined) {
      logger.debug("profile found -> returning None")
      None
    } else {
      val playWebContext = new PlayWebContext(request, playSessionStore)
      val currentClients = getClientFinder().find(config.getClients(), playWebContext, clients)

      logger.debug("currentClients: {}", currentClients)

      val action = try {
        if (startDirectAuthentication(currentClients)) {
          logger.debug("Starting direct authentication")
          val client = currentClients.get(0).asInstanceOf[DirectClient[_ <: Credentials, _ <: CommonProfile]]
          val credentials = client.getCredentials(playWebContext)
          if (credentials != null) {
            val userProfile = credentials.getUserProfile
            if (userProfile != null) {
              setProfile(request, userProfile)
              return Future { None }
            }
          }
          logger.debug("unauthorized")
          unauthorized(playWebContext, currentClients)
        } else if (startAuthentication(playWebContext, currentClients)) {
          logger.debug("Starting authentication")
          saveRequestedUrl(playWebContext, currentClients)
          redirectToIdentityProvider(playWebContext, currentClients)
        } else {
          logger.debug("unauthorized")
          unauthorized(playWebContext, currentClients)
        }
      } catch {
        case e: HttpAction => e
      }

      Some(httpActionAdapter.adapt(action.getCode(), playWebContext).asScala())
    }
  }

  override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] = Future {
    getProfile(request).map(Pac4jSubject(_))
  }

  override def getPermissionsForRole(roleName: String): Future[List[String]] =
    rolePermissionsHandler.getPermissionsForRole(clients, roleName)

  private def getProfile(request: RequestHeader): Option[CommonProfile] = {
    val playWebContext = new PlayWebContext(request, playSessionStore)
    val profileManager = new ProfileManager[CommonProfile](playWebContext)
    profileManager.get(true)
  }

  private def setProfile(request: RequestHeader, profile: CommonProfile): Unit = {
    val playWebContext = new PlayWebContext(request, playSessionStore)
    playWebContext.setRequestAttribute(Pac4jConstants.USER_PROFILES, profile)
  }

  override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] = Future {
    val playWebContext = new PlayWebContext(request, playSessionStore)
    httpActionAdapter.adapt(403, playWebContext).asScala()
  }

  private def httpActionAdapter = config.getHttpActionAdapter().
    asInstanceOf[HttpActionAdapter[play.mvc.Result, PlayWebContext]]

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] =
    throw new TechnicalException("getDynamicResourceHandler() not supported in Pac4jHandler")

  private def startDirectAuthentication(currentClients: java.util.List[Client[_ <: Credentials, _ <: CommonProfile]]): Boolean =
    isNotEmpty(currentClients) && currentClients.get(0).isInstanceOf[DirectClient[_ <: Credentials, _ <: CommonProfile]]
}
