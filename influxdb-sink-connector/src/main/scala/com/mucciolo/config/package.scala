package com.mucciolo

import com.influxdb.LogLevel
import com.influxdb.client.InfluxDBClientOptions

package object config {
  final case class AppConf(connector: ConnectorConf, influx: InfluxDbConf, kafka: KafkaConf)
  final case class ConnectorConf(name: String, measurementName: String, id: String, field: String, time: String)
  final case class InfluxDbConf(url: String, token: String, org: String, bucket: String, logLevel: LogLevel) {
    lazy val asInfluxDBClientOptions: InfluxDBClientOptions = {
      InfluxDBClientOptions.builder()
        .url(url)
        .authenticateToken(token.toCharArray)
        .org(org)
        .bucket(bucket)
        .logLevel(logLevel)
        .build()
    }
  }
  final case class KafkaConf(kafkaBootstrapServers: String, schemaRegistryUrl: String, topic: String, groupId: String)
}
