package org.pac4j.play.scala.deadbolt2

import java.util.Optional

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler}
import be.objectify.deadbolt.scala.models.Subject
import org.pac4j.core.config.Config
import org.pac4j.core.engine.DefaultSecurityLogic
import org.pac4j.core.exception.{HttpAction, TechnicalException}
import org.pac4j.core.http.HttpActionAdapter
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
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
        if (startAuthentication(playWebContext, currentClients)) {
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

  override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] = Future {
    val playWebContext = new PlayWebContext(request, playSessionStore)
    httpActionAdapter.adapt(403, playWebContext).asScala()
  }

  private def httpActionAdapter = config.getHttpActionAdapter().
    asInstanceOf[HttpActionAdapter[play.mvc.Result, PlayWebContext]]

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] =
    throw new TechnicalException("getDynamicResourceHandler() not supported in Pac4jHandler")
}
