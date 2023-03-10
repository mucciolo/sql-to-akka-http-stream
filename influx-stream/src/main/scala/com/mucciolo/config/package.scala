package com.mucciolo

import com.influxdb.client.InfluxDBClientOptions

package object config {
  final case class AppConf(server: HttpServerConf, influx: InfluxDbConf)
  final case class HttpServerConf(host: String, port: Int)
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
}
