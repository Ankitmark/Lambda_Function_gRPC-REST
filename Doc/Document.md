# Homework 3

### Ankit Kumar Singh
### 651288872
### Email: asing200@uic.edu


### Prerequisites to build and run the project

- [SBT](https://www.scala-sbt.org/) installed on your system
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html) installed and configured on your system


### Project Structure

This project uses SBT multi-project build system and consists of the following sub-projects:

1. **root:** The top-level project that aggregates all the other projects but does not contain any source files
2. **Protolib:** Contains the `findlog.proto` file which defines the gRPC service 
3. **GrpcHandler:** Project for AWS Lambda Function that uses Protobuf and JSON as the data-interchange format. *Depends on `Protolib`* . This project implements a common Aws Lambda request handler. Which accepts both ProtoBuffers and JSON content type. Based on the content type invokes the respective handlers and returns the response in ProtoBuff or JSON accordingly.
4. **Client:** This project Contains GRPC and REST client programs for invoking AWS Lambda functions using gRPC. *Depends on `Protolib`*


#### The `Protolib` project

This project contains the `findlog.proto` file which defines the  gRPC service, as below:

```
syntax = "proto3";

option java_package = "com.ankit.protobuf";


// The request message containing the time stamp.
message Input {
  string start = 1;
  string interval = 2;
}

// The response containing the message if log present or not in given timestamp.
message Response {
  string message = 1;
}


// The Findlog service definition.
service Findlog {
  // Sends a input returns response
  rpc Find (Input) returns (Response);
}

```

The project uses [ScalaPB](https://scalapb.github.io/) to generate the stubs for the `Findlogservice` and the related protobuf messages. These stubs are generated automatically when the this project or any of the dependent projects are compiled using `sbt <project-name>/compile`.


#### The `GrpcHandler` project

This project contains one object `Findlogservice` and two classes `S3Reader` and `MyRequestHandler`. The `S3Reader` read the log file in the S3 bucket. `Findlogservice`  implements the logic to find the log with the specified timestamp in the log file and if the log are present return the logs messages between this timestap which contains the patter specified in configuration parameter `Pattern`. While the `MyRequestHandler` class handles the client requests as the lambda function. This AWS Lambda Function that uses Protobuf and JSON as the data-interchange format and  *Depends on `Protolib`* . It implements a common Aws Lambda request handler. Which accepts both ProtoBuffers and JSON content type. Based on the content type invokes the respective handlers and returns the response in ProtoBuff or JSON accordingly. 

The input to the lambda function is an `APIGatewayProxyRequestEvent` object which contain the JSON encoded string representation of the `Input` protobuf in the `body` of the event. Depending on the `Content-Type: ` of the request this function invokes the respective handlers and returns the response in ProtoBuff or JSON accordingly.
The client send either `Content-Type: application/grpc+proto` header or `Content-Type: application/json` to tell how the request will be handled. There are three functions in the project the `handleRequest` function decides the respective handlers based on the content type of header. If the content type is `Content-Type: application/grpc+proto` it passes the request to `runGrpc` similarly if the content type is `Content-Type: application/json` it passes the request to `runREST`.

The output from the lambda function is an `APIGatewayProxyResponseEvent` object. It will contain string reporesentation of the `Response` protobuf in the body of the event.  
The handler itself extracts the request body from the proxy request event, decodes it to constructs the `Input` object from it. This object is passed to the `Findlogservice` to find if the log with input timestamp are present. The `Response` object is then serialized and passed into the body of the response proxy event object.

For deploying this function to AWS Lambda, we just need to issue the command `sbt GrpcHandler/assembly` to package it into a fat jar and upload it on AWS Lambda, selecting **Java 8** as the **runtime** and `com.ankit.GrpcHandlerPac.MyRequestHandler::handleRequest` as the **Handler**. 


#### The `Client` project

This project contains two scala object as the GRPC and REST client. `GrpcClient` and `RestClient` implements the client code to invoke the Lambda functions via API Gateway where  `MyRequestHandler` handles these requests. The start time, the interval and the API Gateway URL is defined in the typesafe config file `Input.conf` from which the client fetch the details and create the requests. To run the client go to the root directory of the project run the command `sbt Client/run` then choose the option by entering 1 or 2 to run the respective client.


**Example of GrpcClient Output**

When logs are not present between the timestamp.

![image](https://user-images.githubusercontent.com/20486562/140591346-0ef51cf9-223d-49af-b42b-322e88b49300.png)

When logs are present between the timestamp.

![image](https://user-images.githubusercontent.com/20486562/140591515-564fc8bc-6f24-42b8-b4c5-236a0057614c.png)

**Example of RestClient Output**

When logs are not present between the timestamp.

![image](https://user-images.githubusercontent.com/20486562/140591630-d1656570-3a5d-4f25-a14c-2f81839976d2.png)

When logs are present between the timestamp.

![image](https://user-images.githubusercontent.com/20486562/140591734-fe65d85b-db16-4c08-9d21-18de6b8fabfb.png)


### Ceating and Deploying the serverless functions on AWS Lambda

Follow the below instructions to deploy the lambda functions on AWS.

1. Create a fat jar of the function using `sbt assembly`
2. 
    ```
    sbt GrpcHandler/assembly
    ```
    
2. Log in to your [AWS Console](https://aws.amazon.com)
3. From **Services**, search for **Lambda** and select it
4. Select **Create function**
5. In the next screen, select **Author from scratch**, and under the basic information section, specify the following and click **Create function**:
    - Function name: `LambdaRequestHandler`
    - Runtime: `Java 8`
6. Under the **Function code** section, specify the following and click on **Save**:
    - Code entry type: `Upload a .zip or .jar file`
    - Runtime: `Java 8`
    - Handler: `com.ankit.GrpcHandlerPac.MyRequestHandler::handleRequest`
    - Function package: Click on **Upload** and browse to `<project-dir>/GrpcHandler/target/scala-2.12/GrpcHandler-assembly-0.1.0-SNAPSHOT.jar`

The lambda function is now deployed on AWS. 


### Creating and Exposing the API for accessing lambda functions using AWS API Gateway

1. Ensure that AWS CLI is installed and configured on your system. Follow this [guide](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html) to know how to do so.
2. Ensure that the user configured with your AWS CLI is having the proper IAM roles and permissions configured for modifying AWS API Gateway.
3. Log in to your [AWS Console](https://aws.amazon.com)
4. From **Services**, search for **API Gateway** and select it.
5. Click on **Create API**
6. In the next screen, choose the following options and click on **Create API**:
    - Protocol: `REST`
    - API Name: `LogFinder`
7. From the **Actions** dropdown, select **Create Resource**, set **Resource Name** and **Resource Path** to `LogFinder`, and click **Create Resource** button
8. From the **Actions** dropdown, select **Create Method**, set the method as `POST`, modify the following options, and click **Save**
    - Integration Type: `Lambda Function`
    - Use Lambda Proxy Integration: Checked
    - Lambda Function: `LambdaRequestHandler` 
9. In your browser, from the **Actions** dropdown, select **Deploy API**
10. Choose **Deployment stage** as `[New Stage]` and **Stage Name** as `prod` and click on **Deploy** button
11. The API is now deployed at the URL mentioned in **API: Dashboardr** page which can be used to invoke the lambda function by running the client as `sbt Client/run`.


### API Gateway URL

The API is deployed using AWS API Gateway at [ https://wyqnmppfe3.execute-api.us-east-1.amazonaws.com/prod/]( https://wyqnmppfe3.execute-api.us-east-1.amazonaws.com/prod/). 

The API can also be used via a client, such as [Postman](https://www.getpostman.com/).

**Sample Payload**

```json
{
"start":"09:03",
"interval":"00:12"
}
```

**Sample Request**

```
curl --location --request POST 'https://wyqnmppfe3.execute-api.us-east-1.amazonaws.com/prod/grpc' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--data-raw '{
    "start":"09:03",
    "interval":"00:12"
}'
```

**Sample Response**

```
HttpResponse(Log Not Found between TimeStamp 09:03 to 09:15
,404,Map(Connection -> Vector(keep-alive), Content-Length -> Vector(47), Content-Type -> Vector(application/json), Date -> Vector(Sat, 06 Nov 2021 00:30:41 GMT), Status -> Vector(HTTP/1.1 404 Not Found), x-amz-apigw-id -> Vector(IWzrqEK9IAMFpnA=), x-amzn-RequestId -> Vector(ef63f48f-c0f3-42ec-9785-58e746d6800c), X-Amzn-Trace-Id -> Vector(Root=1-6185ccb0-70d71b6d1dac33961d556e41;Sampled=0)))
```

**Project presentation video**

Please find the youtube link for the presentation below:

https://youtu.be/6oI64kkwNs4

