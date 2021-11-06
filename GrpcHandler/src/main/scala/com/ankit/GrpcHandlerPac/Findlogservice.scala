package com.ankit.GrpcHandlerPac

import scala.concurrent.Future
import com.ankit.protobuf.findlog.{FindlogGrpc, Input, Response}

import scala.util.matching.Regex
import com.typesafe.config.ConfigFactory
import java.util.Formatter
import com.amazonaws.services.lambda.runtime.{Context}


object Findlogservice extends FindlogGrpc.Findlog {

  private val config = ConfigFactory.load("application")
  val pattern: String = config.getString("AppConfig.Pattern")
  val regx = pattern.r


  /** Sends a input
   */
  override def find(request: Input): Future[Response] = {

    val starttime = request.start
    val interval = request.interval

    val endminute = String.format("%02d", new Integer(starttime.substring(3,5).toInt + interval.substring(3,5).toInt))
    val endhour = String.format("%02d", new Integer(starttime.substring(0,2).toInt + interval.substring(0,2).toInt))

    val endtime = endhour+":"+endminute

    val file = new S3Reader().getlogs()

    var flag = "Log Not Found between TimeStamp " +starttime +" to " + endtime + "\n"

    file.foreach( line => {
      if(line contains(starttime) ){
        flag = "Log Found between TimeStamp " +starttime +" to " + endtime + " Message between timestamp" +
          " containing the pattern: \n" + line +"\n"
      }
      val linehour = line.substring(0, 2)
      val lineminute = line.substring(3, 5)

      val matched = regx.findFirstIn(line)

      if((linehour.toInt <= endhour.toInt && lineminute <= endminute) && (!matched.isEmpty) ){
        flag = flag + line + "\n"
      }
    })
    Future.successful(Response(message = flag))

  }
}

