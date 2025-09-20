package com.dhakarun.adapter.out.cache;

import com.dhakarun.application.port.out.CacheStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisAdapter implements CacheStore {

    private static final Logger log = LoggerFactory.getLogger(RedisAdapter.class);
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisAdapter(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key))
            .flatMap(value -> deserialize(value, type));
    }

    @Override
    public void put(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize value for key {}", key, e);
        }
    }

    @Override
    public void evict(String key) {
        redisTemplate.delete(key);
    }

    private <T> Optional<T> deserialize(String value, Class<T> type) {
        try {
            return Optional.of(objectMapper.readValue(value, type));
        } catch (Exception e) {
            log.warn("Failed to deserialize value for key {}", value, e);
            return Optional.empty();
        }
    }
}
