# SQL to HTTP Stream

A microservice-based end-to-end stream flow.

## Architecture overview
![Architecture Overview](img/architecture-overview.svg?raw=true)

## Dependencies
- Java 11
- Docker Compose
- sbt

## Running locally
1. Place the cursor at the project's root folder
2. Install Debezium Connector
   1. Create a folder named `kafka-connectors` with read-write permissions for Docker container's usage
   2. Download [Debezium Postgres Connector 1.8.1](https://repo1.maven.org/maven2/io/debezium/debezium-connector-postgres/1.8.1.Final/debezium-connector-postgres-1.8.1.Final-plugin.tar.gz) and unpack it to the above folder
3. Run `docker compose up -d` to start Docker Compose
4. Make the POST request at `kafka-connector.http` to configure a Debezium Postgres Connector instance
5. Use sbt to start each of the services `data-crud`, `influxdb-sink-connector` and `influx-stream`

Now you can make the requests exemplified at `data-crud.http` to `data-crud` and observe the streamed average at
`http://localhost:8081/moving-average/{id}?period={seconds}&every={seconds}` to change

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