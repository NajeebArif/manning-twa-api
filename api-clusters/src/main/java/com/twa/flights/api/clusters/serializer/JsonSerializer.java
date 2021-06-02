package com.twa.flights.api.clusters.serializer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.twa.flights.api.clusters.exception.TWAException;
import com.twa.flights.api.clusters.helper.CompressionHelper;

public class JsonSerializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSerializer.class);
    private static final ObjectMapper OBJECT_MAPPER;

    private JsonSerializer() {
        // just to avoid create instances
    }

    static {
        OBJECT_MAPPER = new ObjectMapper().configure(MapperFeature.USE_GETTERS_AS_SETTERS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE).registerModule(new JavaTimeModule());
    }

    public static byte[] serialize(Object object) {
        byte[] compressedJson = null;
        try {
            String json = OBJECT_MAPPER.writeValueAsString(object);
            compressedJson = CompressionHelper.compress(json);
        } catch (IOException e) {
            LOGGER.error("Error serializing string: {}", e.getMessage());
            throw new TWAException("Can't serializing string.");
        }
        return compressedJson;
    }

    public static <T> T deserialize(byte[] raw, Class<T> reference) {
        if (raw == null)
            return null;

        T object = null;
        try {
            String json = CompressionHelper.decompress(raw);
            object = OBJECT_MAPPER.readValue(json, reference);
        } catch (IOException e) {
            LOGGER.error("Error deserializing string: {}", e.getMessage());
            throw new TWAException("Can't deserializing string.");
        }
        return object;
    }

}
