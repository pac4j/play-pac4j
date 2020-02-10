package org.pac4j.play.filters

import akka.stream.Materializer
import javax.inject.{Inject, Singleton}
import org.apache.commons.lang3.StringUtils
import org.pac4j.core.config.Config
import org.pac4j.core.context.Pac4jConstants
import org.pac4j.play.PlayWebContext
import SecurityFilter._
import org.pac4j.play.java.SecureAction
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc._
import play.api.{Configuration, Logger}
import play.mvc
import play.mvc.Http

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

/**
  * Filter on all requests to apply security by the Pac4J framework.
  *
  * Rules for the security filter can be supplied in application.conf. An example is shown below. It
  * consists of a list of filter rules, where the key is a regular expression that will be used to
  * match the path + query string.
  *
  * For each regex key, there are three subkeys: `authorizers`, `clients` and `matchers`. Here you can define the
  * correct values, like you would supply to the `RequireAuthentication` method in controllers. There
  * are two exceptions: `authorizers` can have two special values: `_authenticated_` and `_anonymous_`.
  *
  * `_anonymous_` will disable authentication and authorization for paths matching the regex.
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
class SecurityFilter @Inject()(configuration: Configuration, playSessionStore: PlaySessionStore, config: Config)
                              (implicit val ec: ExecutionContext, val mat: Materializer) extends Filter {
  private val log = Logger(this.getClass)

  private val rules: Seq[Rule] = loadRules(configuration)

  override def apply(nextFilter: RequestHeader => Future[play.api.mvc.Result])
                    (request: RequestHeader): Future[play.api.mvc.Result] = {
    findRule(request).flatMap(_.data) match {
      case Some(rule) =>
        log.debug(s"Authentication needed for ${request.uri}")
        proceedRuleLogic(nextFilter, request, rule)

      case None =>
        log.debug(s"No authentication needed for ${request.uri}")
        nextFilter(request)
    }
  }

  private def proceedRuleLogic(nextFilter: RequestHeader => Future[Result], request: RequestHeader, rule: RuleData): Future[Result] = {
    val webContext = new PlayWebContext(request, playSessionStore)
    val securityAction = new SecureAction(config, playSessionStore)
    val javaContext = webContext.getJavaContext

    def calculateResult(requiresAuthenticationResult: mvc.Result): Future[Result] = {
      val isAuthSucceeded = requiresAuthenticationResult == null
      if (isAuthSucceeded) {
        // we pass the current profiles from the context.args to the request attributes
        // so that the next call to the PlayWebContext.getRequestAttribute works
        val profilesOpt = javaContext.args.asScala.get(Pac4jConstants.USER_PROFILES)
        val requestWithProfiles = profilesOpt match {
          case Some(profiles) => request.addAttr[AnyRef](PlayWebContext.PAC4J_USER_PROFILES.underlying(), profiles)
          case None => request
        }
        nextFilter(requestWithProfiles)
      } else {
        // When the user is not authenticated, the result is one of the following:
        // - forbidden
        // - redirect to IDP
        // - unauthorized
        // Or the future results in an exception
        Future {
          log.info(s"Authentication failed for ${request.uri} with clients ${rule.clients} and authorizers ${rule.authorizers} and matchers ${rule.matchers}. Authentication response code ${requiresAuthenticationResult.status}.")
          createResultSimple(javaContext, requiresAuthenticationResult)
        }
      }
    }

    val futureResult: Future[Result] =
      securityAction
        .internalCall(javaContext, rule.clients, rule.authorizers, rule.matchers, false)
        .toScala
        .flatMap[Result](calculateResult)

    futureResult.andThen { case Failure(ex) => log.error("Exception during authentication procedure", ex) }
  }

  private def findRule(request: RequestHeader): Option[Rule] = {
    val pathNormalized = getNormalizedPath(request)
    rules.find(rule => pathNormalized.matches(rule.pathRegex))
  }

  private def getNormalizedPath(request: RequestHeader): String = {
    val pathPart = removeMultipleSlashed(request.path)
    val queryPart =
      if (StringUtils.isBlank(request.rawQueryString)) ""
      else s"?${request.rawQueryString}"

    pathPart + queryPart
  }

  private def removeMultipleSlashed(path: String): String =
    path.replaceAll("(/){2,}", "$1")

  private def createResultSimple(javaContext: Http.Context, javaResult: play.mvc.Result): play.api.mvc.Result = {
    import scala.collection.convert.decorateAsScala._
    val scalaResult = javaResult.asScala
    scalaResult
      .withHeaders(javaContext.response.getHeaders.asScala.toSeq: _*)
      .withSession(javaContext.session().asScala.data.toSeq: _*)
  }
}

object SecurityFilter {
  private[filters] case class Rule(pathRegex: String, data: Option[RuleData])
  private[filters] case class RuleData(clients: String, authorizers: String, matchers: String)

  private[filters]
  def loadRules(configuration: Configuration): Seq[Rule] = {
    val ruleConfigs = configuration.getOptional[Seq[Configuration]]("pac4j.security.rules").getOrElse(Seq())
    ruleConfigs.map(convertConfToRule)
  }

  private def convertConfToRule(conf: Configuration): Rule = {
    val path = conf.subKeys.head

    val ruleData: Option[RuleData] =
      conf.getOptional[Configuration](s""""$path"""").flatMap { c =>
        val ruleData = RuleData(
          c.getOptional[String]("clients").orNull,
          c.getOptional[String]("authorizers").orNull,
          c.getOptional[String]("matchers").orNull
        )

        ruleData.authorizers match {
          case "_anonymous_" => None
          case "_authenticated_" => Some(ruleData.copy(authorizers = null))
          case _ => Some(ruleData)
        }
      }

    Rule(path.replace("\"", ""), ruleData)
  }
}

