package com.twa.flights.api.provider.alpha.configuration.cache;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.twa.flights.api.provider.alpha.configuration.settings.CacheSettings;
import com.twa.flights.api.provider.alpha.connector.CatalogConnector;

@Configuration
@EnableCaching
public class CacheManagerConfiguration {

    public static final String CATALOG_CITY = "CATALOG_CITY";

    private final CacheConfiguration cacheConfiguration;

    private final CatalogConnector catalogConnector;

    @Autowired
    public CacheManagerConfiguration(final CacheConfiguration cacheConfiguration,
            final CatalogConnector catalogConnector) {
        this.cacheConfiguration = cacheConfiguration;
        this.catalogConnector = catalogConnector;
    }

    @Bean
    public CacheManager cacheManager() {
        CacheSettings cacheCitySettings = cacheConfiguration.getCacheSettings(CATALOG_CITY);

        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(Lists
                .newArrayList(buildCaffeineCache(CATALOG_CITY, cacheCitySettings, catalogConnector::getCityByCode)));

        return simpleCacheManager;
    }

    public static CaffeineCache buildCaffeineCache(String cacheName, CacheSettings settings,
            Function<String, Object> serviceCall) {

        return new CaffeineCache(cacheName,
                Caffeine.newBuilder().refreshAfterWrite(settings.getRefreshAfterWriteTime(), TimeUnit.MINUTES)
                        .expireAfterWrite(settings.getExpireAfterWriteTime(), TimeUnit.MINUTES)
                        .maximumSize(settings.getMaxSize()).build(key -> serviceCall.apply(key.toString())));
    }
}
