lazy val AkkaVersion = "2.6.19"
lazy val AkkaHttpVersion = "10.2.9"
lazy val InfluxDbClientVersion = "6.0.0"
lazy val LogbackVersion = "1.2.11"
lazy val PureConfigVersion = "0.17.1"

lazy val root = (project in file("."))
  .settings(
    organization := "com.mucciolo",
    name := "influx-stream",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.8",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,

      "com.influxdb" %% "influxdb-client-scala" % InfluxDbClientVersion,
      "com.influxdb" % "flux-dsl" % InfluxDbClientVersion,

      "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion
    )
  )
