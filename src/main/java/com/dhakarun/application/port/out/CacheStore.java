package com.dhakarun.application.port.out;

import java.util.Optional;

public interface CacheStore {

    <T> Optional<T> get(String key, Class<T> type);

    void put(String key, Object value);

    void evict(String key);
}
