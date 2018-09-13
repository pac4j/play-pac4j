package org.pac4j.play.filters

import com.typesafe.config.ConfigFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import org.pac4j.play.filters.SecurityFilter.{Rule, RuleData}
import org.scalatest.Matchers._
import play.api.Configuration

import scala.language.postfixOps

//noinspection TypeAnnotation
@RunWith(classOf[BlockJUnit4ClassRunner])
class SecurityFilterTests {

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

}