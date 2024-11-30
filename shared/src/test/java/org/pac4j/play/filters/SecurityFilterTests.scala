package org.pac4j.play.filters

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import org.pac4j.core.client.direct.AnonymousClient
import org.pac4j.core.client.{Clients, MockDirectClient}
import org.pac4j.core.config.Config
import org.pac4j.core.context.FrameworkParameters
import org.pac4j.core.context.session.{SessionStore, SessionStoreFactory}
import org.pac4j.core.engine.DefaultSecurityLogic
import org.pac4j.play.filters.SecurityFilter.{Rule, RuleData}
import org.pac4j.play.http.PlayHttpActionAdapter
import org.pac4j.play.store.PlayCacheSessionStore
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers._
import play.api.Configuration
import play.api.mvc.{RequestHeader, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, status, _}
import play.cache.{DefaultAsyncCacheApi, DefaultSyncCacheApi}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

//noinspection TypeAnnotation
@RunWith(classOf[BlockJUnit4ClassRunner])
class SecurityFilterTests extends ScalaFutures with Results {

  @Test
  def testConvertConfigToRules(): Unit = {
    val config: Configuration = new Configuration(ConfigFactory.load("config/security_filter.conf"))

    SecurityFilter.loadRules(config) shouldBe Seq(
      Rule("/path_anonymous", List(RuleData("AnonymousClient", null, null))),
      Rule("/path_secure_1", List(RuleData("client1,client2", null, null))),
      Rule("/path_secure_3", List(RuleData(null, "authorizer1,authorizer2", null))),
      Rule("/path_secure_4", List(RuleData("client1,client2", "authorizer1,authorizer2", "matcher1,matcher2")))
    )
  }

  @Test
  def testThatSecurityFilterBlocksUnauthorizedRequests(): Unit = {
    implicit val ec = scala.concurrent.ExecutionContext.global
    implicit val as = ActorSystem("text-actor-system")
    implicit val mat: ActorMaterializer = ActorMaterializer()

    val securityFilter = prepareSecurityFilter(
      """
        |pac4j.security.rules = [
        |  {
        |    "/path_anonymous" = {
        |      "clients" = "AnonymousClient"
        |      "authorizers" = "none"
        |    }
        |  }, {
        |    "/path_secure" = {
        |      clients = "client1"
        |    }
        |  }, {
        |    "/path_secure_2/.*" = {
        |      clients = "client1"
        |    }
        |  }
        |]
      """.stripMargin
    )

    status(tryFilterApply(securityFilter, "/path_secure", POST)) shouldBe 401
    status(tryFilterApply(securityFilter, "/path_secure_2/any_path_", POST)) shouldBe 401

    status(tryFilterApply(securityFilter, "/path_anonymous", POST)) shouldBe 200
    status(tryFilterApply(securityFilter, "any/other/path", POST)) shouldBe 200
  }

  @Test
  def testThatSecurityFilterBlocksUnauthorizedRequestsWithMatchers(): Unit = {
    implicit val ec = scala.concurrent.ExecutionContext.global
    implicit val as = ActorSystem("text-actor-system")
    implicit val mat: ActorMaterializer = ActorMaterializer()

    val securityFilter = prepareSecurityFilter(
      """
        |pac4j.security.rules = [
        |  {
        |    "/path_secure" = {
        |      "clients" = "AnonymousClient"
        |      "authorizers" = "none"
        |      "matchers" = "put"
        |    }
        |  },
        |  {
        |    "/path_secure" = {
        |      "clients" = "AnonymousClient"
        |      "authorizers" = "none"
        |      "matchers" = "get"
        |    }
        |  }, {
        |    "/path_secure" = {
        |      clients = "client1"
        |      "matchers" = "post"
        |    }
        |  }, {
        |    "/path_secure" = {
        |      "clients" = "AnonymousClient"
        |    }
        |  }, {
        |    "/path_secure/deeper" = {
        |      clients = "client1"
        |    }
        |  }
        |]
      """.stripMargin
    )

    status(tryFilterApply(securityFilter, "/path_secure", GET)) shouldBe 200
    status(tryFilterApply(securityFilter, "/path_secure", POST)) shouldBe 401
    status(tryFilterApply(securityFilter, "/path_secure/deeper", GET)) shouldBe 401
    status(tryFilterApply(securityFilter, "/path_secure/deeper", POST)) shouldBe 401
  }

  private def tryFilterApply(securityFilter: SecurityFilter, path: String, method: String): Future[Result] = {
      val nextFilter = (_: RequestHeader) => Future.successful(Ok("ok"))
      val testRequest: RequestHeader = FakeRequest(method, path)
      securityFilter.apply(nextFilter)(testRequest)
    }

  private def prepareSecurityFilter(configString: String)
                                   (implicit ec: ExecutionContext, mat: Materializer): SecurityFilter = {
    val pac4jConfig = new Config
    pac4jConfig.setSecurityLogic(DefaultSecurityLogic.INSTANCE)
    pac4jConfig.setHttpActionAdapter(PlayHttpActionAdapter.INSTANCE)
    pac4jConfig.setClients(new Clients(new MockDirectClient("client1"), AnonymousClient.INSTANCE))

    val playSessionStore = new PlayCacheSessionStore(new DefaultSyncCacheApi(new DefaultAsyncCacheApi(new MockInMemoryAsyncCacheApi())))
    pac4jConfig.setSessionStoreFactory(new SessionStoreFactory {
      override def newSessionStore(parameters: FrameworkParameters): SessionStore = playSessionStore
    });
    val playConfig = new Configuration(ConfigFactory.parseString(configString))

    new SecurityFilter(playConfig, pac4jConfig)
  }
}
