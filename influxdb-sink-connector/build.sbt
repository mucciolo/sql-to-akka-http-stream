lazy val AkkaVersion = "2.7.0"
lazy val AkkaStreamVersion = "4.0.0"
lazy val InfluxDbClientVersion = "6.7.0"
lazy val ApicurioRegistryVersion = "2.2.2.Final"
lazy val LogbackVersion = "1.4.5"
lazy val PureConfigVersion = "0.17.2"

lazy val root = (project in file("."))
  .settings(
    organization := "com.mucciolo",
    name := "influxdb-sink-connector",
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.13.10",
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
