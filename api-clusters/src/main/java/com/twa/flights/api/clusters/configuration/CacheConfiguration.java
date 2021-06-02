package com.twa.flights.api.clusters.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

//@Configuration
public class CacheConfiguration {

    public static final String API_CATALOG_CACHE = "apiCatalogCache";

    // @Bean
    public Caffeine caffeine() {
        return Caffeine.newBuilder().maximumSize(500).expireAfterAccess(600, TimeUnit.SECONDS);
    }

    // @Bean
    public CacheManager cacheManager(Caffeine caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeine);
        caffeineCacheManager.setCacheNames(Arrays.asList(API_CATALOG_CACHE));
        return caffeineCacheManager;
    }

}
