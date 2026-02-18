package com.usermanagement.service;

/**
 * Service interface for caching operations.
 */
public interface CacheService {

    /**
     * Store a value in cache.
     */
    void set(String key, Object value);

    /**
     * Retrieve a value from cache.
     */
    <T> T get(String key, Class<T> type);

    /**
     * Delete a value from cache.
     */
    void delete(String key);

    /**
     * Check if key exists in cache.
     */
    boolean exists(String key);

    /**
     * Clear all cache entries.
     */
    void clear();
}
