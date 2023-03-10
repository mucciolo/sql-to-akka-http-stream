lazy val AkkaVersion = "2.7.0"
lazy val AkkaHttpVersion = "10.5.0"
lazy val InfluxDbClientVersion = "6.7.0"
lazy val LogbackVersion = "1.4.5"
lazy val PureConfigVersion = "0.17.2"

lazy val root = (project in file("."))
  .settings(
    organization := "com.mucciolo",
    name := "influx-stream",
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.13.10",
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
