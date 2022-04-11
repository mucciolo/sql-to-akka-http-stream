val AkkaVersion = "2.6.19"
val AkkaStreamVersion = "3.0.0"
val InfluxDbClientVersion = "5.0.0"
val ApicurioRegistryVersion = "2.2.2.Final"
val LogbackVersion = "1.2.11"
val PureConfigVersion = "0.17.1"

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
