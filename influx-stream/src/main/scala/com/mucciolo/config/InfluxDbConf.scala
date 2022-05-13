package com.mucciolo.config

import com.influxdb.client.InfluxDBClientOptions

final case class InfluxDbConf(url: String, token: String, org: String, bucket: String) {

  lazy val asInfluxDBClientOptions: InfluxDBClientOptions = {
    InfluxDBClientOptions.builder()
      .url(url)
      .authenticateToken(token.toCharArray)
      .org(org)
      .bucket(bucket)
      .build()
  }

}
