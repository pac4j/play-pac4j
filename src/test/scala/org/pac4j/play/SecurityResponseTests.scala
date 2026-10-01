package org.pac4j.play

import com.typesafe.config.ConfigFactory
import org.apache.pekko.stream.Materializer
import org.junit.Assert._
import org.junit.Test
import org.mockito.Mockito.mock
import org.pac4j.core.client.direct.AnonymousClient
import org.pac4j.core.config.Config
import org.pac4j.core.context.{CallContext, Cookie}
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.filters.SecurityFilter
import org.pac4j.play.scala.{AuthenticatedRequest, SecureAction}
import org.pac4j.play.store.{NoOpDataEncrypter, PlayCookieSessionStore}
import play.api.Configuration
import play.api.mvc.{AnyContent, Result, Results}
import play.api.test.FakeRequest
import play.mvc.Http

import _root_.scala.concurrent.{Await, ExecutionContext, Future}
import _root_.scala.concurrent.duration._

final class SecurityResponseTests extends Results {
  private implicit val ec: ExecutionContext = ExecutionContext.global
  private implicit val mat: Materializer = mock(classOf[Materializer])
  private type UserRequest[A] = AuthenticatedRequest[CommonProfile, A]

  @Test
  def testFilterPropagatesResponseChanges(): Unit = {
    val (config, store) = securityConfig()
    config.addMatcher("first", context => changeResponse(context, "first"))
    val filter = securityFilter(config, Seq("first"))
    val result = await(filter(request => {
      assertEquals("first", state(store, new PlayWebContext(request)))
      assertEquals("first", new PlayWebContext(request).getRequestAttribute("state").orElseThrow())
      Future.successful(Ok("ok"))
    })(FakeRequest("GET", "/secure")))
    assertResponse(result, store, "first")
  }

  @Test
  def testScalaActionPropagatesResponseChanges(): Unit = {
    val (config, store) = securityConfig()
    config.addMatcher("first", context => changeResponse(context, "first"))
    val action = SecureAction[CommonProfile, AnyContent, UserRequest](
      "AnonymousClient", "none", "first", null, config)
    val result = await(action.invokeBlock(FakeRequest("GET", "/secure"), request => {
      assertEquals("first", state(store, new PlayWebContext(request)))
      assertEquals("first", new PlayWebContext(request).getRequestAttribute("state").orElseThrow())
      Future.successful(Ok("ok"))
    }))
    assertResponse(result, store, "first")
  }

  @Test
  def testChainedRulesSeePreviousStateAndKeepAllResponses(): Unit = {
    val (config, store) = securityConfig()
    config.addMatcher("first", context => changeResponse(context, "first"))
    config.addMatcher("second", context => {
      assertEquals("first", state(store, context.webContext().asInstanceOf[PlayWebContext]))
      assertEquals("first", context.webContext().getRequestAttribute("state").orElseThrow())
      changeResponse(context, "second")
    })
    val filter = securityFilter(config, Seq("first", "second"))
    val result = await(filter(request => {
      assertEquals("second", state(store, new PlayWebContext(request)))
      Future.successful(Ok("ok").withSession(request.session + ("controller" -> "value")))
    })(FakeRequest("GET", "/secure")))
    assertResponse(result, store, "second")
    assertEquals("first", result.asJava.header("X-first").orElseThrow())
    assertEquals("first", result.asJava.cookie("first").orElseThrow().value())
    assertEquals("value", result.asJava.session().get("controller").orElseThrow())
  }

  @Test
  def testLaterDenialKeepsPreviousResponseChanges(): Unit = {
    val (config, store) = securityConfig()
    config.addMatcher("first", context => changeResponse(context, "first"))
    config.addMatcher("second", context => changeResponse(context, "second"))
    config.addAuthorizer("deny", (_, _, _) => false)
    val filter = securityFilter(config, Seq("first", "second"), "deny")
    val result = await(filter(_ => {
      fail("A denied request must not reach the controller")
      Future.successful(Ok("unexpected"))
    })(FakeRequest("GET", "/secure")))
    assertEquals(403, result.header.status)
    assertResponse(result, store, "second")
    assertEquals("first", result.asJava.header("X-first").orElseThrow())
    assertEquals("first", result.asJava.cookie("first").orElseThrow().value())
  }

  @Test
  def testUnchangedFilterDoesNotCreateSession(): Unit = {
    val (config, _) = securityConfig()
    config.addMatcher("unchanged", _ => true)
    val result = await(securityFilter(config, Seq("unchanged"))(_ => Future.successful(Ok("ok")))(
      FakeRequest("GET", "/secure")))
    assertNull(result.asJava.session())
    assertFalse(result.asJava.header("Set-Cookie").isPresent)
  }

  private def securityConfig(): (Config, PlayCookieSessionStore) = {
    val config = new Config(AnonymousClient.INSTANCE)
    val store = new PlayCookieSessionStore(new NoOpDataEncrypter)
    config.setSessionStoreFactory(_ => store)
    (config, store)
  }

  private def securityFilter(config: Config, matchers: Seq[String], lastAuthorizer: String = "none"): SecurityFilter = {
    val rules = matchers.zipWithIndex.map { rule =>
      val matcher = rule._1
      val index = rule._2
      val authorizer = if (index == matchers.size - 1) lastAuthorizer else "none"
      s"""{ "/secure" = { clients = "AnonymousClient", authorizers = "$authorizer", matchers = "$matcher" } }"""
    }.mkString(",")
    new SecurityFilter(Configuration(ConfigFactory.parseString(s"pac4j.security.rules = [$rules]")), config)
  }

  private def changeResponse(context: CallContext, value: String): Boolean = {
    context.webContext().setResponseHeader(s"X-$value", value)
    context.webContext().setResponseHeader("X-State", value)
    context.webContext().setResponseContentType(s"application/$value")
    context.webContext().addResponseCookie(new Cookie(value, value))
    context.webContext().addResponseCookie(new Cookie("shared", value))
    context.sessionStore().set(context.webContext(), "state", value)
    context.webContext().setRequestAttribute("state", value)
    true
  }

  private def state(store: PlayCookieSessionStore, context: PlayWebContext): Object =
    store.get(context, "state").orElseThrow()

  private def assertResponse(result: Result, store: PlayCookieSessionStore, value: String): Unit = {
    assertEquals(value, result.asJava.header(s"X-$value").orElseThrow())
    assertEquals(value, result.asJava.header("X-State").orElseThrow())
    assertEquals(value, result.asJava.cookie(value).orElseThrow().value())
    assertEquals(value, result.asJava.cookie("shared").orElseThrow().value())
    assertEquals(Some(s"application/$value"), result.body.contentType)
    val nextRequest = new Http.RequestBuilder().session(result.asJava.session().data()).build()
    assertEquals(value, state(store, new PlayWebContext(nextRequest)))
  }

  private def await(result: Future[Result]): Result = Await.result(result, 5.seconds)
}
