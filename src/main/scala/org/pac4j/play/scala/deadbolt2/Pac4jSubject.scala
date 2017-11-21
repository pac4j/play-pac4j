package org.pac4j.play.scala.deadbolt2

import scala.collection.JavaConversions._

import be.objectify.deadbolt.scala.models.{Permission, Role, Subject}
import org.pac4j.core.profile.CommonProfile


case class Pac4jRole(
  name: String
) extends Role


case class Pac4jPermission(
  value: String
) extends Permission


case class Pac4jSubject(
  identifier: String,

  roles: List[Role],
  permissions: List[Permission],

  name: String,
  avatarURL: Option[String]
) extends Subject


object Pac4jSubject {
  def apply(profile: CommonProfile): Pac4jSubject = {
    Pac4jSubject(
      profile.getId,

      profile.getRoles.map(Pac4jRole(_)).toList,
      profile.getPermissions.map(Pac4jPermission(_)).toList,

      profile.getDisplayName,
      Option(profile.getPictureUrl).map(_.toString())
    )
  }
}
