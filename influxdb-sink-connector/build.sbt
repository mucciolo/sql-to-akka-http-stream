lazy val AkkaVersion = "2.6.19"
lazy val AkkaStreamVersion = "3.0.0"
lazy val InfluxDbClientVersion = "6.0.0"
lazy val ApicurioRegistryVersion = "2.2.2.Final"
lazy val LogbackVersion = "1.2.11"
lazy val PureConfigVersion = "0.17.1"

lazy val root = (project in file("."))
  .settings(
    organization := "com.mucciolo",
    name := "influxdb-sink-connector",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.8",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"     %% "akka-stream-kafka"                    % AkkaStreamVersion,
      "com.typesafe.akka"     %% "akka-stream"                          % AkkaVersion,
      "com.typesafe.akka"     %% "akka-actor-typed"                     % AkkaVersion,
      "com.typesafe.akka"     %% "akka-slf4j"                           % AkkaVersion,

      "com.influxdb"          %% "influxdb-client-scala"                % InfluxDbClientVersion,
      "ch.qos.logback"        %  "logback-classic"                      % LogbackVersion,
      "io.apicurio"           %  "apicurio-registry-serdes-avro-serde"  % ApicurioRegistryVersion,
      "com.github.pureconfig" %% "pureconfig"                           % PureConfigVersion
    )
  )
