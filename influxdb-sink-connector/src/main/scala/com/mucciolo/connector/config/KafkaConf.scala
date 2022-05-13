package com.mucciolo.connector.config

final case class KafkaConf(kafkaBootstrapServers: String, schemaRegistryUrl: String, topic: String, groupId: String)
