package com.usermanagement.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis implementation of CacheService.
 * Provides distributed caching capabilities using Redis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService implements CacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.redis.ttl-seconds:3600}")
    private long ttlSeconds;

    @Override
    public void set(String key, Object value) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, ttlSeconds, TimeUnit.SECONDS);
            log.debug("Cached value for key: {}", key);
        } catch (JsonProcessingException e) {
            log.error("Error serializing value for key: {}", key, e);
        }
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        try {
            String jsonValue = redisTemplate.opsForValue().get(key);
            if (jsonValue != null) {
                log.debug("Cache hit for key: {}", key);
                return objectMapper.readValue(jsonValue, type);
            }
            log.debug("Cache miss for key: {}", key);
            return null;
        } catch (JsonProcessingException e) {
            log.error("Error deserializing value for key: {}", key, e);
            return null;
        }
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
        log.debug("Deleted cache key: {}", key);
    }

    @Override
    public boolean exists(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    @Override
    public void clear() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
        log.info("Cleared all cache entries");
    }
}
