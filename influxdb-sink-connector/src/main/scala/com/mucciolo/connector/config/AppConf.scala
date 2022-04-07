package com.mucciolo.connector.config

case class AppConf(connectorName: String, influx: InfluxDbConf, kafka: KafkaConf)
