package com.usermanagement.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.domain.entity.User;
import com.usermanagement.event.UserCreatedEvent;
import com.usermanagement.event.UserDeletedEvent;
import com.usermanagement.event.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private static final String USER_EVENTS_TOPIC = "user-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishUserCreatedEvent(User user) {
        try {
            UserCreatedEvent event = UserCreatedEvent.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(USER_EVENTS_TOPIC, "user.created", eventJson);
            log.info("Published UserCreatedEvent for user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to publish UserCreatedEvent", e);
        }
    }

    public void publishUserUpdatedEvent(User user) {
        try {
            UserUpdatedEvent event = UserUpdatedEvent.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(USER_EVENTS_TOPIC, "user.updated", eventJson);
            log.info("Published UserUpdatedEvent for user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to publish UserUpdatedEvent", e);
        }
    }

    public void publishUserDeletedEvent(User user) {
        try {
            UserDeletedEvent event = UserDeletedEvent.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(USER_EVENTS_TOPIC, "user.deleted", eventJson);
            log.info("Published UserDeletedEvent for user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to publish UserDeletedEvent", e);
        }
    }
}
