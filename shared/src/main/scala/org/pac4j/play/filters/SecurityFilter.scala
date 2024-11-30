package org.pac4j.play.filters

import org.apache.pekko.stream.Materializer
import org.pac4j.core.adapter.FrameworkAdapter
import org.pac4j.core.config.Config
import org.pac4j.core.util.CommonHelper
import org.pac4j.play.PlayWebContext
import org.pac4j.play.context.PlayFrameworkParameters
import org.pac4j.play.filters.SecurityFilter._
import org.pac4j.play.java.SecureAction
import org.pac4j.play.result.PlayWebContextResultHolder
import play.api.mvc._
import play.api.{Configuration, Logger}
import play.mvc

import javax.inject.{Inject, Singleton}
import scala.jdk.FutureConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure
import scala.util.matching.Regex

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
class SecurityFilter @Inject()(configuration: Configuration, config: Config)
                              (implicit val ec: ExecutionContext, val mat: Materializer) extends Filter {
  private val log = Logger(this.getClass)

  private val rules: Seq[Rule] = loadRules(configuration)

  override def apply(nextFilter: RequestHeader => Future[play.api.mvc.Result])
                    (request: RequestHeader): Future[play.api.mvc.Result] = {
    findRule(request).map(_.data) match {
      case Some(rule :: remainingRules) =>
        log.debug(s"Authentication needed for ${request.uri}")
        proceedRuleLogic(nextFilter, request, rule, remainingRules)

      case _ =>
        log.debug(s"No authentication needed for ${request.uri}")
        nextFilter(request)
    }
  }

  private def proceedRuleLogic(
    nextFilter: RequestHeader => Future[Result],
    request: RequestHeader,
    rule: RuleData,
    remainingRules: Seq[RuleData]
  ): Future[Result] = {

    FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config)

    val parameters = new PlayFrameworkParameters(request)
    val webContext = config.getWebContextFactory().newContext(parameters).asInstanceOf[PlayWebContext]
    val securityAction = new SecureAction(config)

    def checkSecurity(request: RequestHeader, rule: RuleData, remainingRules: Seq[RuleData]): Future[Result] =
      securityAction
        .call(parameters, rule.clients, rule.authorizers, rule.matchers)
        .asScala
        .flatMap { secureActionResult =>
          if (secureActionResult.isInstanceOf[PlayWebContextResultHolder]) {
            val newCtx = secureActionResult.asInstanceOf[PlayWebContextResultHolder].getPlayWebContext
            val newRequest = newCtx.supplementRequest(request.asJava).asScala

            remainingRules match {
              case Nil => nextFilter(newRequest)
              case head :: tail => checkSecurity(newRequest, head, tail)
            }
          } else {
            // When the user is not authenticated, the result is one of the following:
            // - forbidden
            // - redirect to IDP
            // - unauthorized
            // Or the future results in an exception
            Future.successful {
              log.info(s"Authentication failed for ${request.uri} with clients ${rule.clients} and authorizers ${rule.authorizers} and matchers ${rule.matchers}. Authentication response code ${secureActionResult.status}.")
              secureActionResult.asScala
            }
          }
        }

    checkSecurity(request, rule, remainingRules).andThen { case Failure(ex) => log.error("Exception during authentication procedure", ex) }
  }

  private def findRule(request: RequestHeader): Option[Rule] = {
    val pathNormalized = getNormalizedPath(request)
    rules.find(rule => rule.compiledRegex.matches(pathNormalized))
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
  private[filters] case class Rule(pathRegex: String, data: List[RuleData]) {
    val compiledRegex = pathRegex.r

    def mergeData(other: Rule) = this.copy(data = this.data ++ other.data)
  }
  private[filters] case class RuleData(clients: String, authorizers: String, matchers: String)

  private[filters]
  def loadRules(configuration: Configuration): Seq[Rule] = {
    val ruleConfigs = configuration.getOptional[Seq[Configuration]]("pac4j.security.rules").getOrElse(Seq())
    ruleConfigs
      .map(convertConfToRule)
      // coalesce adjacent rules with the exact same path
      .foldLeft(List.empty[Rule]) {
        case (Nil, rule) => List(rule)
        case (head :: tail, rule) if head.pathRegex == rule.pathRegex => head.mergeData(rule) :: tail
        case (list, rule) => rule :: list
      }
      .reverse
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

    Rule(path.replace("\"", ""), ruleData.toList)
  }
}
