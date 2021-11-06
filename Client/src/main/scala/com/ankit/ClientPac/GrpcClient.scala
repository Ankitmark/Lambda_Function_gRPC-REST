package com.ankit.ClientPac

import com.ankit.protobuf.findlog.{FindlogGrpc, Input, Response}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import scalaj.http.Http
import scalapb.json4s.JsonFormat

/**
 * GRPC Client which sends the request to Aws Lambda with application/grpc+proto content type.
 *
 */


object GrpcClient extends App with LazyLogging {


    // Initialize API Gateway URL from the config file
    val config = ConfigFactory.load("Input")
    val url: String = config.getString("ApiInput.api-gateway-url")

    val start: String = config.getString("ApiInput.timeinput.start")
    val interval: String = config.getString("ApiInput.timeinput.interval")

    val expression = new Input(start, interval)

    logger.trace(s"Find(expression: $expression")
    val expressionJson = JsonFormat.toJsonString(expression)
    println(expressionJson)

    // Make POST request to calculator API Gateway
    val request = Http(url)
      .headers(Map(
        "Content-Type" -> "application/grpc+proto",
        "Accept" -> "application/grpc+proto"
      ))
      .timeout(connTimeoutMs = 5000, readTimeoutMs = 10000) // So that request doesn't time out for Lambda cold starts
      .postData(expressionJson)

    logger.debug(s"Making HTTP request: $request")
    val response = request.asString
    logger.debug(s"Got response: $response")

    // Parse response from API to protobuf Response object
    //val responseMessage = Response.parseFrom(response.body)
    logger.debug(s"Response message: $response")

    // Return the result
    println(response)

}