package com.twa.flights.api.clusters.serializer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import com.twa.flights.api.clusters.exception.TWAException;
import com.twa.flights.api.clusters.helper.CompressionHelper;

@Component
public class StringSerializer implements RedisSerializer<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringSerializer.class);

    public byte[] serialize(String value) {
        byte[] compressedString;
        try {
            compressedString = CompressionHelper.compress(value);
        } catch (IOException e) {
            LOGGER.error("Error serializing string: {}", e.getMessage());
            throw new TWAException("Can't serializing string.");
        }
        return compressedString;
    }

    public String deserialize(byte[] raw) {
        String string;
        try {
            string = CompressionHelper.decompress(raw);
        } catch (IOException e) {
            LOGGER.error("Error deserializing string: {}", e.getMessage());
            throw new TWAException("Can't deserializing string.");
        }
        return string;
    }

}
