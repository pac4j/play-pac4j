package org.pac4j.play.scala

import javax.inject.Inject
import org.pac4j.core.authorization.checker.DefaultAuthorizationChecker
import org.pac4j.core.config.Config
import org.pac4j.core.profile.{CommonProfile, ProfileManager, UserProfile}
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc.RequestHeader

import scala.collection.JavaConverters._

/**
  * This is a helper which can be used to access the current user profile from a twirl template.
  * Idea comes from https://github.com/vidma who mentioned this in ticket: https://github.com/pac4j/play-pac4j/issues/181
  *
  * Usage: You have to inject this via CDI for example in your SecurityModule:  bind(classOf[Pac4jScalaTemplateHelper[CommonProfile]])
  *
  * In your controller add this to the constructor: implicit val pac4jTemplateHelper: Pac4jScalaTemplateHelper[CommonProfile]
  *
  * In your Action the request value must be declared implicit.
  *
  * In your templates add this as implicit constructor values: (implicit pac4jScalaTemplateHelper: Pac4jScalaTemplateHelper[CommonProfile], requestHeader: RequestHeader)
  *
  * For an example take a look in the play-pac4j-scala-demo: ApplicationWithScalaHelper
  *
  *
  * @author Sebastian Hardt
  *
  * @since 6.0.0
  */
class Pac4jScalaTemplateHelper[P<:CommonProfile] @Inject()(playSessionStore: PlaySessionStore, config: Config)  {

  private val authorizationChecker = new DefaultAuthorizationChecker()

  /**
    * Gets all current profiles the current user has in its session
    * @param request the request of the user
    * @return a [[List]] of [[CommonProfile]]
    */
  def getCurrentProfiles(implicit request: RequestHeader): List[P] = {
    val profileManager = createProfileManager
    profileManager.getAll(true).asScala.toList
  }


  /**
    * Gets the current [[CommonProfile]] from the session
    * @param request the request of the user
    * @return [[Option]] with None when there is no current user and Some with the [[CommonProfile]] of the user
    */
  def getCurrentProfile(implicit request: RequestHeader): Option[P] = {

    val profileManager = createProfileManager

    val javaProfileOptional = profileManager.get(true)

    if(javaProfileOptional.isPresent) {
      Option.apply(javaProfileOptional.get())
    } else {
      None
    }
  }

  /**
    * Creates a [[ProfileManager]]
    * @param request the request of the user
    * @return the newly creates [[ProfileManager]]
    */
  def createProfileManager(implicit request: RequestHeader) : ProfileManager[P] = {
    val webContext = new PlayWebContext(request, playSessionStore)
    new ProfileManager[P](webContext)
  }

  def isAuthorized(authorizers: String)(implicit request: RequestHeader): Boolean = {
    val context = new PlayWebContext(request, playSessionStore)
    val profiles = getCurrentProfiles.asInstanceOf[List[UserProfile]].asJava
    authorizationChecker.isAuthorized(context, profiles, authorizers, config.getAuthorizers)
  }
}
