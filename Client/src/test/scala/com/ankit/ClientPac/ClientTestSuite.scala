package com.ankit.ClientPac


import com.ankit.ClientPac.GrpcClient.config
import com.typesafe.config.ConfigFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class ClientTestSuite extends AnyFlatSpec with Matchers {
  val config = ConfigFactory.load("Input")
  behavior of "configuration parameters module"

  it should "obtain the correct API gateway " in {
    config.getString("ApiInput.api-gateway-url") shouldBe "https://wyqnmppfe3.execute-api.us-east-1.amazonaws.com/prod/grpc"
  }

  it should "obtain the  start time" in {
    config.getString("ApiInput.timeinput.start") shouldBe "10:03"
  }

}
