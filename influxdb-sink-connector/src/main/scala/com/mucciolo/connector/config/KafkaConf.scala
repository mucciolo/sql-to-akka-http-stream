package com.mucciolo.connector.config

case class KafkaConf(kafkaBootstrapServers: String, schemaRegistryUrl: String, topic: String, groupId: String)
