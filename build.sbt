name := "Lambda_GRPC"

version := "0.1"

scalaVersion := "2.13.7"

// Dependency definitions-----------------------------------------------///

// Typesafe Configuration Library
lazy val typesafeConfig = "com.typesafe" % "config" % "1.4.1"

// Logback logging framework
lazy val logback = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
  "ch.qos.logback" % "logback-classic" % "1.2.6",
  "org.gnieh" % "logback-config" % "0.4.0"
)

// Scalatest testing framework
lazy val scalatest = "org.scalatest" %% "scalatest" % "3.2.9" % "test"

// ScalaPB JSON to Protobuf convertor
lazy val json4s = "com.thesamet.scalapb" %% "scalapb-json4s" % "0.12.0"

// ScalaJ HTTP Library
lazy val scalaJHTTP = "org.scalaj" %% "scalaj-http" % "2.4.2"



// Merge strategy to avoid deduplicate errors
lazy val assemblySettings =
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
  }

// ScalaPB gRPC runtime
lazy val grpcRuntime = "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion

// Project that contains the *.proto files
lazy val Protolib = (project in file("Protolib"))
  .settings(
    // ScalaPB configuration
    Compile / PB.targets := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    ),
    libraryDependencies ++= Seq(
      grpcRuntime,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
    )
  )

// Project containing client programs for invoking AWS Lambda functions using gRPC, and also the "main" client program
lazy val Client = (project in file("Client"))
  .settings(
    libraryDependencies ++= Seq(
      typesafeConfig,
      json4s,
      scalaJHTTP,
      scalatest,
    ) ++ logback
  )
  .dependsOn(Protolib)


// AWS Lambda SDK
lazy val awsCore = "com.amazonaws" % "aws-lambda-java-core" % "1.2.1"
lazy val awsEvents = "com.amazonaws" % "aws-lambda-java-events" % "3.10.0"
lazy val awsVersion = "2.17.66"

// Project for AWS Lambda Function that uses Protobuf as the data-interchange format
lazy val GrpcHandler = (project in file("GrpcHandler"))
  .settings(
    assemblySettings,
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
    libraryDependencies ++= Seq(
      awsCore,
      awsEvents,
      json4s,
      scalatest,
      "software.amazon.awssdk" % "s3" % awsVersion,
      "software.amazon.awssdk" % "lambda" % awsVersion,
      "com.typesafe" % "config" % "1.4.1",
      "com.github.mifmif" % "generex" % "1.0.2"

    )
  ).dependsOn(Protolib)

lazy val root = (project in file("."))
  .aggregate(Protolib, GrpcHandler, Client)
