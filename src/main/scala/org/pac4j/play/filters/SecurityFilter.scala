package org.pac4j.play.filters

import java.net.URI
import java.util.Collections
import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.PlayWebContext
import org.pac4j.play.java.SecureAction
import org.pac4j.play.scala.Security
import org.pac4j.play.store.PlaySessionStore
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import play.api.{Configuration, Logger}
import play.mvc.Http
import play.libs.concurrent.HttpExecutionContext

import scala.collection.JavaConversions._
import scala.compat.java8.FutureConverters._
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


/**
  * Filter on all requests to apply security by the Pac4J framework.
  *
  * Rules for the security filter can be supplied in application.conf. An example is shown below. It
  * consists of a list of filter rules, where the key is a regular expression that will be used to
  * match the url.
  *
  * For each regex key, there are two subkeys: `authorizers` and `clients`. Here you can define the
  * correct values, like you would supply to the `RequireAuthentication` method in controllers. There
  * two exceptions: `authorizers` can have two special values: `_authenticated_` and `_anonymous_`.
  *
  * `_anonymous_` will disable authentication and authorization for urls matching the regex.
  * `_authenticated_` will require authentication, but will set clients and authorizers both to `null`.
  *
  * Rules are traversed and applied from top to bottom. The first matching rule will define which clients and authorizers
  * are used. When not provided, the value will be `null`.
  *
  * @example {{{
  * security.rules = [
  *   # Admin pages need a special authorizer and do not support login via Twitter.
  *   {"/admin/.*" = {
  *     authorizers = "admin"
  *     clients = "FormClient"
  *   }}
  *   # Rules for the REST services. These don't specify a client and will return 401
  *   # when not authenticated.
  *   {"/restservices/.*" = {
  *     authorizers = "_authenticated_"
  *   }}
  *   # The login page needs to be publicly accessible.
  *   {"/login.html" = {
  *     authorizers = "_anonymous_"
  *   }}
  *   # 'Catch all' rule to make sure the whole application stays secure.
  *   {".*" = {
  *     authorizers = "_authenticated_"
  *     clients = "FormClient,TwitterClient"
  *   }}
  * ]
  * }}}
  * @author Hugo Valk
  * @since 2.1.0
  */
@Singleton
class SecurityFilter @Inject()(val mat:Materializer, configuration: Configuration, val playSessionStore: PlaySessionStore, val config: Config, override val ec: HttpExecutionContext) extends Filter with Security[CommonProfile] {

  val log = Logger(this.getClass)

  val rules = configuration.getConfigList("pac4j.security.rules")
    .getOrElse(Collections.emptyList())

  override def apply(nextFilter: (RequestHeader) => Future[play.api.mvc.Result])
                    (request: RequestHeader): Future[play.api.mvc.Result] = {
    findRule(request) match {
      case Some(rule) =>
        log.debug(s"Authentication needed for ${request.uri}")
        val webContext = new PlayWebContext(request, playSessionStore)
        val securityAction = new SecureAction(config, playSessionStore, ec)
        val javaContext = webContext.getJavaContext
        val futureResult = securityAction.internalCall(javaContext, rule.clients, rule.authorizers, false)
          .toScala
          .flatMap[play.api.mvc.Result]{ requiresAuthenticationResult =>
          if (requiresAuthenticationResult == null)
            // If the authentication succeeds, the action result is null
            nextFilter(request)
          else {
            /**
              * When the user is not authenticated, the result is one of the following:
              * - forbidden
              * - redirect to IDP
              * - unauthorized
              * Or the future results in an exception
              */
            Future {
              log.info(s"Authentication failed for ${request.uri} with clients ${rule.clients} and authorizers ${rule.authorizers}. Authentication response code ${requiresAuthenticationResult.status}.")
              createResultSimple(javaContext, result(requiresAuthenticationResult.asScala())(request))
            }
          }
        }
        futureResult.onFailure{case x => log.error("Exception during authentication procedure", x)}

        futureResult

      case None =>
        log.debug(s"No authentication needed for ${request.uri}")
        nextFilter(request)
    }
  }

  /**
    * Includes a redirect uri in session depending on the validity of the incoming uri
    * @param requiresAuthenticationResult the result with no authentication
    * @param request the current request
    * @return a result with an loginRedirect session if uri is valid
    */
  def result(requiresAuthenticationResult: Result)(implicit request: RequestHeader) : Result = {
    // throws an URISyntaxException if given a string that violates RFC2396
    Try(new URI(request.uri)) match {
      case Success(uri) => {
        log.debug(s"redirect url set to ${request.uri}.")
        requiresAuthenticationResult.addingToSession(("loginRedirect" -> uri.getPath))
      }
      case Failure(exc) => {
        log.debug(s"Exception thrown when creating an URI obj from ${request.uri}. Exception: ${exc.toString}")
        requiresAuthenticationResult
      }
    }
  }

  def findRule(request: RequestHeader): Option[Rule] =
    rules.find { rule =>
      val key = rule.subKeys.head
      val regex = key.replace("\"", "")
      request.uri.matches(regex)
    }.flatMap(configurationToRule)

  def configurationToRule(c: Configuration): Option[Rule] = {
    c.getConfig("\"" + c.subKeys.head + "\"").flatMap { rule =>
      val res = new Rule(rule.getString("clients").orNull, rule.getString("authorizers").orNull)
      if (res.authorizers == "_anonymous_")
        None
      else if (res.authorizers == "_authenticated_")
        Some(res.copy(authorizers = null))
      else Some(res)
    }
  }

  case class Rule(clients: String, authorizers: String)

  def createResultSimple(javaContext: Http.Context, scalaResult: Result): Result = {
    import scala.collection.convert.decorateAsScala._
    scalaResult.withHeaders(javaContext.response.getHeaders.asScala.toSeq: _*)
  }
}
