package org.pac4j.play.scala.deadbolt2

import scala.collection.mutable.Map
import scala.concurrent.ExecutionContext
import be.objectify.deadbolt.scala.{DeadboltHandler, HandlerKey}
import be.objectify.deadbolt.scala.cache.HandlerCache
import javax.inject.{Inject, Singleton}
import org.pac4j.core.config.Config
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.util.CommonHelper

case class ClientsHandlerKey(clients: String) extends HandlerKey

/**
  * @author Zenkie Zhu
  * @since 4.1.0
  */
@Singleton
class Pac4jHandlerCache @Inject()(config: Config, sessionStore: SessionStore, roleHandler: Pac4jRoleHandler)(implicit ec: ExecutionContext) extends HandlerCache {

  private val handlers: Map[HandlerKey, DeadboltHandler] = Map()

  private val defaultHandlerKey = ClientsHandlerKey("defaultHandler")

  private val defaultHandler = new Pac4jHandler(config, null, sessionStore, roleHandler);

  handlers += (defaultHandlerKey -> defaultHandler)

  override def apply(): DeadboltHandler = defaultHandler

  override def apply(handlerKey: HandlerKey): DeadboltHandler = {
    handlers.get(handlerKey).
      getOrElse(getOrBuildHandler(handlerKey).get)
  }

  protected def getOrBuildHandler(handlerKey: HandlerKey): Option[DeadboltHandler] = synchronized {
    handlers.get(handlerKey).orElse {
      Some(handlerKey).
        filter(_.isInstanceOf[ClientsHandlerKey]).
        map(_.asInstanceOf[ClientsHandlerKey]).
        map(_.clients).
        map(new Pac4jHandler(config, _, sessionStore, roleHandler)).
        orElse(buildCustomHandler(handlerKey)).
        map { handler =>
          handlers += (handlerKey -> handler)
          handler
        }
    }
  }

  protected def buildCustomHandler(handlerKey: HandlerKey): Option[DeadboltHandler] = None

  override def toString() = CommonHelper.toNiceString(this.getClass(), "handlers", handlers, "config", config,
    "executionContext", ec, "playSessionStore", sessionStore)
}
