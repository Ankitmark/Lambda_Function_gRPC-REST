package com.ankit.GrpcHandlerPac

import scala.concurrent.Await
import scala.concurrent.duration._
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.ankit.protobuf.findlog.{FindlogGrpc, Input, Response}

import java.util.Base64
import scala.jdk.CollectionConverters._

import scalapb.json4s.JsonFormat


/**
 * Aws Lambda request handler. Which accepts both ProtoBuffers and JSON content type.
 * Based on the content type invokes the respective handlers and returns the response in ProtoBuff or JSON
 * accordingly
 */

class MyRequestHandler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {


  /**
   * Default Request handler that accepts both Proto and JSON content type
   *
   */
  override def handleRequest(request: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {

    val logger = context.getLogger

    logger.log("Client Request received...")

    logger.log("Identifying request type....")

//    logger.log(request.getBody)
//    logger.log(request.getHeaders.toString)
//    logger.log(JsonFormat.fromJsonString[Input](request.getBody).toString)
//    logger.log(request.getHeaders.containsKey("Content-Type").toString)
//    logger.log(request.getHeaders.keySet().toString)

    return request.getHeaders.get("Content-Type") match {
      case "application/grpc+proto" => runGrpc(request, context)
      case _ => runRest(request, context)
    }
  }
  /**
   * GRPC Request handler that accepts both Proto and JSON content type
   *
   */

  def runGrpc(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {

    // Get AWS Lambda Logger
    val logger = context.getLogger

    logger.log("GRPC Request...")

    logger.log("Request Body:\n" + input.toString)
    logger.log("Raw Body:\n" + input.getBody)

    // Convert JSON string and use ScalaPB to construct the `Input`
    val expression = JsonFormat.fromJsonString[Input](input.getBody)
    logger.log(s"Input expression : $expression")


    // find log using gRPC service
    logger.log(s"finding log using gRPC service")
    val result = Await.result(Findlogservice.find(expression), 60.seconds)
    //logger.log(s"Result: $result")

    //  the response protobuf
    val output = result.message
    logger.log(s"Output: $output")

    // Send the response
    if(output contains("Not Found")){
      new APIGatewayProxyResponseEvent()
        .withStatusCode(404)
        .withHeaders(Map("Content-Type" -> "application/grpc+proto").asJava)
        .withBody(output)
    }
    else{
      new APIGatewayProxyResponseEvent()
        .withStatusCode(200)
        .withHeaders(Map("Content-Type" -> "application/grpc+proto").asJava)
        .withBody(output)
    }

  }

  /**
   *
   * @param request API GateWay request with JSON content type and Query params
   * @param context lambda context
   * @return APIGateway Response
   */
  def runRest(request: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {

    val lambdaLogger = context.getLogger

    lambdaLogger.log("REST request....")

    // Convert JSON string and use ScalaPB to construct the `Input`
    val expression = JsonFormat.fromJsonString[Input](request.getBody)
    lambdaLogger.log(s"Input expression: $expression")


    // find log using REST service
    lambdaLogger.log(s"finding log using REST service")
    val result = Await.result(Findlogservice.find(expression), 60.seconds)
    //lambdaLogger.log(s"Result: $result")

    //  the response protobuf
    val output = result.message
    lambdaLogger.log(s"Output: $output")

    // Send the response
    if(output contains("Not Found")){
      new APIGatewayProxyResponseEvent()
        .withStatusCode(404)
        .withHeaders(Map("Content-Type" -> "application/json").asJava)
        .withBody(output)
    }
    else{
      new APIGatewayProxyResponseEvent()
        .withStatusCode(200)
        .withHeaders(Map("Content-Type" -> "application/json").asJava)
        .withBody(output)
    }

  }

}


