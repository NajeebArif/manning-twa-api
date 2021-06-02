package com.twa.flights.api.provider.beta.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.twa.flights.api.provider.beta.configuration.settings.RedisSettings;
import com.twa.flights.api.provider.beta.dto.CityDTO;

@Configuration
@ConfigurationProperties
public class RedisConfiguration {

    private RedisSettings redis;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redis.getHost(),
                redis.getPort());
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisTemplate<String, CityDTO> redisTemplate() {
        RedisTemplate<String, CityDTO> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());

        return redisTemplate;
    }

    public RedisSettings getRedis() {
        return redis;
    }

    public void setRedis(RedisSettings redis) {
        this.redis = redis;
    }
}