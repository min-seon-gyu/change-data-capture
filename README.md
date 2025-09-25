# Charge Data Capture (CDC) Example Project

이 프로젝트는 Spring Boot, Debezium, Kafka, MySQL을 사용하여 변경 데이터 캡처(Change Data Capture, CDC)를 구현한 예제입니다.

데이터베이스(MySQL)의 `example` 테이블에서 발생하는 데이터 변경(INSERT, UPDATE, DELETE)을 Debezium이 감지하여 Kafka 토픽으로 메시지를 보냅니다. Spring Boot 애플리케이션은 해당 토픽을 구독(subscribe)하여 변경 이벤트를 실시간으로 수신하고 처리합니다.

## 아키텍처

```
+-----------------+       +-----------------+       +-----------------+       +---------------------+
|                 |       |                 |       |                 |       |                     |
|    MySQL DB     |------>|    Debezium     |------>|      Kafka      |------>|   Spring Boot App   |
|                 |       |                 |       |                 |       |                     |
+-----------------+       +-----------------+       +-----------------+       +---------------------+
```

1.  **MySQL**: 데이터 변경이 발생하는 원본 데이터베이스입니다. `binlog`가 활성화되어 있어야 합니다.
2.  **Debezium (Kafka Connect)**: MySQL의 `binlog`를 읽어 데이터 변경 이벤트를 캡처하고 Kafka 토픽으로 전송합니다.
3.  **Kafka**: Debezium이 보낸 변경 이벤트 메시지를 저장하고 전달하는 메시지 브로커입니다.
4.  **Spring Boot App**: Kafka 토픽을 구독하여 변경 이벤트를 수신하고 `CdcEventConsumer`를 통해 실시간으로 비즈니스 로직을 처리합니다.

## 주요 기술 스택

-   **Backend**: Java 21, Spring Boot 3.5.6
-   **Database**: MySQL 8.0
-   **Messaging**: Kafka 7.5.3
-   **CDC**: Debezium 2.5

## 실행 방법

### 1. Spring Boot 애플리케이션 실행

IDE 또는 Gradle을 사용하여 Spring Boot 애플리케이션(`ChargeDataCaptureApplication`)을 실행합니다. `spring-boot-docker-compose` 의존성 덕분에 애플리케이션이 실행될 때 `compose.yaml`에 정의된 `mysql`, `kafka`, `debezium-connect` 서비스들이 자동으로 시작되고 연결됩니다.

### 2. MySQL 권한 설정

Debezium이 MySQL의 변경 이벤트를 읽기 위해서는 특정 권한이 필요합니다. MySQL 컨테이너가 실행된 후, 다음 명령어를 사용하여 `spring` 사용자에게 필요한 권한을 부여합니다.

```bash
docker exec -it cdc_mysql mysql -u root -pverysecret -e "GRANT RELOAD, PROCESS, SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON example.* TO 'spring'@'%';"
```

### 3. Debezium MySQL 커넥터 등록

Spring Boot 애플리케이션이 완전히 시작되어 Docker Compose 서비스들이 실행된 후 아래 `curl` 명령어를 실행하여 Debezium이 MySQL 데이터베이스를 모니터링하도록 커넥터를 등록합니다.

```bash
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d '{
  "name": "example-mysql-connector",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "tasks.max": "1",
    "database.hostname": "mysql",
    "database.port": "3306",
    "database.user": "spring",
    "database.password": "secret",
    "database.server.id": "1",
    "database.server.name": "dbserver1",
    "database.include.list": "example",
    "table.include.list": "example.example",
    "database.history.kafka.bootstrap.servers": "kafka:29092",
    "database.history.kafka.topic": "dbhistory.example",
    "topic.prefix": "dbserver1",
    "schema.history.internal.kafka.bootstrap.servers": "kafka:29092",
    "schema.history.internal.kafka.topic": "schemahistory.example",
    "include.schema.changes": "true"
  }
}'
```

-   **성공 응답**: `HTTP/1.1 201 Created`
-   등록된 커넥터 확인: `curl http://localhost:8083/connectors/`

### 4. CDC 이벤트 트리거 및 확인

애플리케이션이 실행되면, API를 호출하여 `example` 테이블에 데이터를 추가하거나 변경해서 CDC 이벤트를 발생시킬 수 있습니다.

**데이터 생성 (INSERT)**

```bash
curl -X POST http://localhost:8080/example -H "Content-Type:application/json" -d '{"name": "test-user", "description": "This is a test."}'
```

위 명령어를 실행하면 `example` 테이블에 새로운 데이터가 삽입됩니다. Debezium은 이 변경을 감지하여 Kafka로 메시지를 보내고, Spring Boot 애플리케이션의 콘솔 로그에 다음과 같은 CDC 이벤트 메시지가 출력됩니다.

```
INFO --- [ntainer#0-0-C-1] d.c.example.CdcEventConsumer           : Received CDC event for customers table: {"schema":{...},"payload":{"before":null,"after":{"id":1,"name":"test-user","description":"This is a test."},"source":{...},"op":"c",...}}
```
