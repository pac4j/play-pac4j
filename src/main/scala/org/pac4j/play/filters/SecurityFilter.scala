/*
  Copyright 2012 - 2015 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.play.filters

import java.util.Collections
import javax.inject.{Inject, Singleton}

import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.PlayWebContext
import org.pac4j.play.java.RequiresAuthenticationAction
import org.pac4j.play.scala.Security
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.api.{Configuration, Logger}
import play.core.j.JavaHelpers

import scala.collection.JavaConversions._
import scala.concurrent.Future

/**
  * Filter on all requests to apply security by the Pac4J framework.
  *
  * Rules for the security filter can be supplied in application.conf. An example is shown below. It
  * consists of a list of filter rules, where the key is a regular expression that will be used to
  * match the url. Make sure that the / is escaped by \\ to make a valid regular expression.
  *
  * For each regex key, there are two subkeys: `authorizers` and `clients`. Here you can define the
  * correct values, like you would supply to the `RequireAuthentication` method in controllers. There
  * two exceptions: `authorizers` can have two special values: `_authenticated_` and `_anonymous_`.
  *
  * `_anonymous_` will disable authentication and authorization for urls matching the regex.
  * `_authenticated_` will require authentication, but will set clients and authorizers both to `null`.
  *
  * Rules are applied top to bottom. The first matching rule will define which clients and authorizers
  * are used. When not provided, the value will be `null`.
  *
  * @example {{{
  *           security.rules = [
  *             # Admin pages need a special authorizer and do not support login via Twitter.
  *             {"/admin/.*" = {
  *               authorizers = "admin"
  *               clients = "FormClient"
  *             }}
  *             # Rules for the REST services. These don't specify a client and will return 401
  *             # when not authenticated.
  *             {"/restservices/.*" = {
  *               authorizers = "_authenticated_"
  *             }}
  *             # The login page needs to be publicly accessible.
  *             {"/login.html" = {
  *               authorizers = "_anonymous_"
  *             }}
  *             # 'Catch all' rule to make sure the whole application stays secure.
  *             {".*" = {
  *               authorizers = "_authenticated_"
  *               clients = "FormClient,TwitterClient"
  *             }}
  *           ]
  *          }}}
  *
  * @author Hugo Valk
  *
  * @since 2.1.0
  */
@Singleton
class SecurityFilter @Inject()(configuration: Configuration) extends Filter with Security[CommonProfile] {

  val log = Logger(this.getClass)

  val rules = configuration.getConfigList("security.rules")
    .getOrElse(Collections.emptyList())

  override def apply(nextFilter: (RequestHeader) => Future[Result])
                    (request: RequestHeader): Future[Result] = {
    findRule(request) match {
      case Some(rule) =>
        log.debug(s"Authentication needed for ${request.uri}")
        val webContext = new PlayWebContext(request, config.getSessionStore)
        val requiresAuthenticationAction = new RequiresAuthenticationAction(config)
        val javaContext = webContext.getJavaContext
        requiresAuthenticationAction.internalCall(javaContext, rule.clientNames, rule.authorizerNames).wrapped().flatMap[play.api.mvc.Result](r =>
          if (r == null) {
            nextFilter(request)
          } else {
            Future {
              log.info(s"Authentication failed for ${request.uri} with clients ${rule.clientNames} and authorizers ${rule.authorizerNames}")
              JavaHelpers.createResult(javaContext, r)
            }
          }
        )
      case None =>
        log.debug(s"No authentication needed for ${request.uri}")
        nextFilter(request)
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
      if (res.authorizerNames == "_anonymous_")
        None
      else if (res.authorizerNames == "_authenticated_")
        Some(res.copy(authorizerNames = null))
      else Some(res)
    }
  }

  case class Rule(clientNames: String, authorizerNames: String)

}

