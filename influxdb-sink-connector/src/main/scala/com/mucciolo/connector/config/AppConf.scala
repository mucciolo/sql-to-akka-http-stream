package com.mucciolo.connector.config

final case class AppConf(connectorName: String, influx: InfluxDbConf, kafka: KafkaConf)
