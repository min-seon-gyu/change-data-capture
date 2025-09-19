package demo.chargedatacapture.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CdcEventConsumer {

    /**
     * 'customers' 테이블의 변경 이벤트를 구독합니다.
     * * @KafkaListener 어노테이션이 붙은 이 메소드는
     * 지정된 토픽에 새로운 메시지가 들어오면 자동으로 실행됩니다.
     *
     * 토픽 이름은 Debezium 커넥터 설정의 아래 규칙에 따라 결정됩니다.
     * [topic.prefix].[데이터베이스이름].[테이블이름]
     * -> "dbserver1.example.example"
     *
     * @param message Debezium이 생성한 JSON 형식의 메시지 (String)
     */
    @KafkaListener(topics = "dbserver1.example.example", groupId = "charge-data-capture-group")
    public void consumeCustomerEvents(String message) {
        log.info("Received CDC event for customers table: {}", message);

        // --- 여기에 비즈니스 로직을 구현합니다 ---
        // 1. 수신한 JSON 메시지를 객체(DTO)로 파싱합니다.
        // 2. 'op' 필드 값('c', 'u', 'd')에 따라 어떤 변경인지 확인합니다.
        // 3. 변경된 데이터를 사용하여 다른 시스템의 API를 호출하거나,
        //    검색 엔진(Elasticsearch)에 데이터를 동기화하거나,
        //    데이터 캐시를 업데이트하는 등의 후속 작업을 처리합니다.
    }
}
