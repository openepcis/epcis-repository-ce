package io.openepcis.generated.capture.producers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hubspot.jinjava.Jinjava;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class ApplicationProducers {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(new Jdk8Module())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .registerModule(new JavaTimeModule());

    @Produces
    @ApplicationScoped
    public ObjectMapper createObjectMapper() {
        return objectMapper;
    }

    @Produces
    @ApplicationScoped
    public Jinjava jinjava() {
        return new Jinjava();
    }

    @Produces
    @ApplicationScoped
    public JsonFactory createJsonFactory(final ObjectMapper objectMapper) {
        return objectMapper.getFactory();
    }
}