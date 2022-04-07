val AkkaVersion = "2.6.19"

lazy val root = (project in file("."))
  .settings(
    organization := "com.mucciolo",
    name := "influxdb-sink-connector",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.8",
    libraryDependencies ++= Seq(
      "com.influxdb" %% "influxdb-client-scala" % "5.0.0",
      "com.typesafe.akka" %% "akka-stream-kafka" % "3.0.0",
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.11",
      "io.apicurio" % "apicurio-registry-serdes-avro-serde" % "2.2.2.Final",
      "com.github.pureconfig" %% "pureconfig" % "0.17.1"
    )
  )
