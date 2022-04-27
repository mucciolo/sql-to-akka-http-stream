# SQL to HTTP Stream

A microservice-based end-to-end stream flow.

## Architecture overview
![Architecture Overview](img/architecture-overview.svg?raw=true)

## Dependencies
- Java 11
- Docker Compose 2

## Running locally
With the cursor at the project's root folder
1. Download [Debezium Postgres Connector](https://repo1.maven.org/maven2/io/debezium/debezium-connector-postgres/1.8.1.Final/debezium-connector-postgres-1.8.1.Final-plugin.tar.gz) and unpack it to a folder named `kafka-connectors` with read-write permission for the Docker user
2. Run `docker compose up -d` to start Docker Compose
3. Make the POST request at the first line of `kafka-connector.http` to configure a Debezium Postgres Connector instance
4. Use `sbt run` to start `data-crud`, `influxdb-sink-connector` and `influx-stream`

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