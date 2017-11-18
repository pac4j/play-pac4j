package org.pac4j.play.scala.deadbolt2

import scala.concurrent.{ExecutionContext, Future}

trait Pac4jRoleHandler {
  def getPermissionsForRole(clients: String, roleName: String)
    (implicit ec: ExecutionContext): Future[List[String]]
}

class SimpleRoleHandler extends Pac4jRoleHandler {
  override def getPermissionsForRole(clients: String, roleName: String)
    (implicit ec: ExecutionContext): Future[List[String]] = Future(Nil)
}
