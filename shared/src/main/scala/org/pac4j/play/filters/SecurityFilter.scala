package org.pac4j.play.filters

import akka.stream.Materializer
import javax.inject.{Inject, Singleton}
import org.pac4j.core.config.Config
import SecurityFilter._
import org.pac4j.core.util.CommonHelper
import org.pac4j.play.PlayWebContext
import org.pac4j.play.java.SecureAction
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc._
import play.api.{Configuration, Logger}
import play.mvc

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
  * For each regex key, there are three subkeys: `clients`, `authorizers` and `matchers`. Here you can define the
  * correct values, like you would supply to the `SecureAction` method in controllers.
  *
  * Rules are traversed and applied from top to bottom. The first matching rule will define which clients, authorizers and matchers
  * are used. When not provided, the value will be `null`.
  *
  * @example {{{
  * security.rules = [
  *   # Admin pages need a special authorizer and login is done via a form page.
  *   {"/admin/.*" = {
  *     clients = "FormClient"
  *     authorizers = "admin"
  *   }}
  *   # Rules for the REST services. These don't specify a client and will return 401
  *   # when not authenticated.
  *   {"/restservices/.*" = {
  *   }}
  *   # The login page needs to be publicly accessible.
  *   {"/login.html" = {
  *     clients = "AnonymousClient"
  *   }}
  *   # No security must be applied on the callback endpoint.
  *   {"/callback.*" = {
  *   }}
  *   # 'Catch all' rule to make sure the whole application stays secure.
  *   {".*" = {
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

    def calculateResult(secureActionResult: mvc.Result): Future[Result] = {
      val isAuthSucceeded = secureActionResult == null
      if (isAuthSucceeded) {
        nextFilter(webContext.supplementRequest(request.asJava).asScala)
      } else {
        // When the user is not authenticated, the result is one of the following:
        // - forbidden
        // - redirect to IDP
        // - unauthorized
        // Or the future results in an exception
        Future {
          log.info(s"Authentication failed for ${request.uri} with clients ${rule.clients} and authorizers ${rule.authorizers} and matchers ${rule.matchers}. Authentication response code ${secureActionResult.status}.")
          secureActionResult.asScala
        }
      }
    }

    val futureResult: Future[Result] =
      securityAction
        .call(webContext, rule.clients, rule.authorizers, rule.matchers, false)
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
      if (CommonHelper.isBlank(request.rawQueryString)) ""
      else s"?${request.rawQueryString}"

    pathPart + queryPart
  }

  private def removeMultipleSlashed(path: String): String =
    path.replaceAll("(/){2,}", "$1")
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
        val clients = c.getOptional[String]("clients").orNull
        val authorizers = c.getOptional[String]("authorizers").orNull
        val matchers = c.getOptional[String]("matchers").orNull

        if (clients != null || authorizers != null || matchers != null) {
          Some(RuleData(clients, authorizers, matchers))
        } else {
          None
        }
      }

    Rule(path.replace("\"", ""), ruleData)
  }
}
