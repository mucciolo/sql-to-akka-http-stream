package com.mucciolo.config

import com.influxdb.client.InfluxDBClientOptions

final case class InfluxDbConf(url: String, username: String, password: String, org: String, bucket: String) {

  lazy val asInfluxDBClientOptions: InfluxDBClientOptions = {
    InfluxDBClientOptions.builder()
      .url(url)
      .authenticate(username, password.toCharArray)
      .org(org)
      .bucket(bucket)
      .build()
  }

}