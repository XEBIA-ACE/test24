package com.usermanagement.service;

import java.util.Map;

/**
 * Service interface for Kafka message production.
 */
public interface KafkaProducerService {

    /**
     * Send user event to Kafka topic.
     */
    void sendUserEvent(Map<String, Object> event);
}
