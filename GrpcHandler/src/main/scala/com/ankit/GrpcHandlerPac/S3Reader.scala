package com.ankit.GrpcHandlerPac

import com.typesafe.config.ConfigFactory
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import com.amazonaws.services.lambda.runtime.{Context}

import java.io.{BufferedReader, InputStreamReader}
import scala.collection.mutable.ListBuffer

class S3Reader {

  private val config = ConfigFactory.load("application")

  def getlogs(): List[String]= {

    val BUCKET_NAME: String = config.getString("AppConfig.BUCKET_NAME")
    val FILE_NAME: String = config.getString("AppConfig.FILE_NAME")


    val amazonS3Client = S3Client.builder().region(Region.US_EAST_1)
    .httpClient(ApacheHttpClient.builder().build()).build()

    val objRequest:GetObjectRequest = GetObjectRequest.builder().bucket(BUCKET_NAME)
      .key(FILE_NAME).build()


    // download file and read line by line
    val obj = amazonS3Client.getObjectAsBytes(objRequest).asInputStream()
    val reader = new BufferedReader(new InputStreamReader(obj))
    var line = reader.readLine
    val logs = new ListBuffer[String]()

//    line.foreach( l => {
//      logs += line
//      line = reader.readLine
//    })

    while (line != null) {
      val msg = line.split(" - ",2)
      if(!(msg.length < 2)){
        logs += line
      }
      line = reader.readLine
    }
    logs.toList

  }
}
