package com.mucciolo.connector.config

import com.influxdb.LogLevel
import com.influxdb.client.InfluxDBClientOptions

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
