package com.usermanagement.service.impl;

import com.usermanagement.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of KafkaProducerService.
 * Publishes user events to Kafka topics for event-driven architecture.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @Value("${app.kafka.topics.user-events:user-events}")
    private String userEventsTopic;

    @Override
    public void sendUserEvent(Map<String, Object> event) {
        String userId = (String) event.get("userId");
        log.debug("Sending user event to Kafka: {}", event.get("eventType"));

        CompletableFuture<SendResult<String, Map<String, Object>>> future =
                kafkaTemplate.send(userEventsTopic, userId, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("User event sent successfully: {} - Offset: {}",
                        event.get("eventType"),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send user event: {}", event.get("eventType"), ex);
            }
        });
    }
}
