package com.mucciolo.connector.config

final case class AppConf(connector: ConnectorConf, influx: InfluxDbConf, kafka: KafkaConf)
