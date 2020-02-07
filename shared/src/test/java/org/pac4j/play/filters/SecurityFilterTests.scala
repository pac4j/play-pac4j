package org.pac4j.play.filters

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import org.pac4j.core.client.{Clients, MockDirectClient}
import org.pac4j.core.config.Config
import org.pac4j.core.engine.DefaultSecurityLogic
import org.pac4j.play.filters.SecurityFilter.{Rule, RuleData}
import org.pac4j.play.http.PlayHttpActionAdapter
import org.pac4j.play.store.PlayCacheSessionStore
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures
import play.api.Configuration
import play.api.mvc.{RequestHeader, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, status, _}
import play.cache.{DefaultAsyncCacheApi, DefaultSyncCacheApi}

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

//noinspection TypeAnnotation
@RunWith(classOf[BlockJUnit4ClassRunner])
class SecurityFilterTests extends ScalaFutures with Results {

  @Test
  def testConvertConfigToRules(): Unit = {
    val config: Configuration = new Configuration(ConfigFactory.load("config/security_filter.conf"))

    SecurityFilter.loadRules(config) shouldBe Seq(
      Rule("/path_anonymous", None),
      Rule("/path_secure_1", Some(RuleData("client1,client2", null, null))),
      Rule("/path_secure_2", Some(RuleData("client1,client2", null, null))),
      Rule("/path_secure_3", Some(RuleData(null, "authorizer1,authorizer2", null))),
      Rule("/path_secure_4", Some(RuleData("client1,client2", "authorizer1,authorizer2", "matcher1,matcher2")))
    )
  }

  @Test
  def testThatSecurityFilterBlocksUnauthorizedRequests(): Unit = {
    implicit val ec = scala.concurrent.ExecutionContext.global
    implicit val as = ActorSystem("text-actor-system")
    implicit val mat = ActorMaterializer()

    val securityFilter = prepareSecurityFilter(
      """
        |pac4j.security.rules = [
        |  {
        |    "/path_anonymous" = {
        |      authorizers = "_anonymous_"
        |    }
        |  }, {
        |    "/path_anonymous_2/.*" = {
        |      authorizers = "_anonymous_"
        |    }
        |  }, {
        |    "/path_secure" = {
        |      authorizers = "_authenticated_"
        |      clients = "client1"
        |    }
        |  }, {
        |    "/path_secure_2/.*" = {
        |      authorizers = "_authenticated_"
        |      clients = "client1"
        |    }
        |  }
        |]
      """.stripMargin
    )

    def tryFilterApply(path: String): Future[Result] = {
      val nextFilter = (_: RequestHeader) => Future.successful(Ok("ok"))
      val testRequest: RequestHeader = FakeRequest(POST, path)
      securityFilter.apply(nextFilter)(testRequest)
    }

    status(tryFilterApply("/path_secure")) shouldBe 401
    status(tryFilterApply("/path_secure_2/any_path_")) shouldBe 401

    status(tryFilterApply("/path_anonymous")) shouldBe 200
    status(tryFilterApply("/path_anonymous_2/any_path")) shouldBe 200
    status(tryFilterApply("any/other/path")) shouldBe 200
  }

  private def prepareSecurityFilter(configString: String)
                                   (implicit ec: ExecutionContext, mat: Materializer): SecurityFilter = {
    val pack4jConfig = new Config
    pack4jConfig.setSecurityLogic(DefaultSecurityLogic.INSTANCE)
    pack4jConfig.setHttpActionAdapter(PlayHttpActionAdapter.INSTANCE)
    pack4jConfig.setClients(new Clients(Seq(
      new MockDirectClient("client1")
    )))

    val playSessionStore = new PlayCacheSessionStore(new DefaultSyncCacheApi(new DefaultAsyncCacheApi(new MockInMemoryAsyncCacheApi())))
    val playConfig = new Configuration(ConfigFactory.parseString(configString))

    new SecurityFilter(playConfig, playSessionStore, pack4jConfig)
  }

}
