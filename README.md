# SQL to HTTP Stream [under development]

A microservice-based reactive end-to-end flow.

## Architecture overview
![Architecture Overview](img/architecture-overview.svg?raw=true)

## Dependencies
- Java 11+

## Libraries 
| Service                 | Libraries                                            |
|-------------------------|------------------------------------------------------|
| data-crud               | http4s (with Ember + Cats) + Doobie + Flyway + Circe |
| influxdb-sink-connector | Akka Stream + Alpakka                                |
| influx-stream           | Akka Stream + Akka HTTP                              |

## Ports
| Service                 | Port |
|-------------------------|------|
| Postgres                | 5432 |
| InfluxDB                | 8086 |
| Zookeeper               | 2187 |
| Kafka                   | 9092 |
| Schema Registry         | 9093 |
| Kafka Connect           | 8083 |
| Kafka UI                | 8090 |
| data-crud               | 8080 |
| influxdb-sink-connector | N/A  |
| influx-stream           | 8081 |