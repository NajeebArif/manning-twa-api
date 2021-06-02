package com.twa.flights.api.clusters.configuration.cache;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.google.common.collect.Lists;
import com.twa.flights.api.clusters.configuration.settings.CacheSettings;
import com.twa.flights.api.clusters.serializer.CitySerializer;

@Configuration
@EnableCaching
public class CacheManagerConfiguration {

    public static final String CATALOG_CITY = "CATALOG_CITY";

    private final CacheConfiguration cacheConfiguration;
    private final JedisConnectionFactory catalogJedisConnectionFactory;
    private final CitySerializer citySerializer;

    @Autowired
    public CacheManagerConfiguration(final CacheConfiguration cacheConfiguration,
            final JedisConnectionFactory catalogJedisConnectionFactory, final CitySerializer citySerializer) {
        this.cacheConfiguration = cacheConfiguration;
        this.catalogJedisConnectionFactory = catalogJedisConnectionFactory;
        this.citySerializer = citySerializer;
    }

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(Lists.newArrayList(RedisCacheManager.builder(catalogJedisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration()).build().getCache(CATALOG_CITY)));

        return simpleCacheManager;
    }

    private RedisCacheConfiguration redisCacheConfiguration() {
        CacheSettings cacheCitySettings = cacheConfiguration.getCacheSettings(CATALOG_CITY);
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(citySerializer))
                .entryTtl(Duration.ofMinutes(cacheCitySettings.getExpireAfterWriteTime()));
    }
}
