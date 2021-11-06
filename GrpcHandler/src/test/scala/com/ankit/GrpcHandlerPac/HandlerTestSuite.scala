package com.ankit.GrpcHandlerPac

import com.typesafe.config.ConfigFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HandlerTestSuite extends AnyFlatSpec with Matchers {
  val config = ConfigFactory.load("application")
  behavior of "configuration parameters module"

  it should "obtain the message pattern " in {
    config.getString("AppConfig.Pattern") shouldBe "([a-c][e-g][0-3]|[A-Z][5-9][f-w]){5,15}"
  }

  it should "obtain the S3 Bucket name" in {
    config.getString("AppConfig.BUCKET_NAME") shouldBe "homework3ankit"
  }

  it should "obtain the file name of S3 Bucket " in {
    config.getString("AppConfig.FILE_NAME") shouldBe "LogFileGenerator.2021-11-05.log"
  }

}
