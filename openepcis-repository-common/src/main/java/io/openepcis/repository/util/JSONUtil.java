/*
 * Copyright 2022-2024 benelog GmbH & Co. KG
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package io.openepcis.repository.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.openepcis.core.exception.MarshallingException;
import io.openepcis.model.epcis.exception.ExceptionMessages;

import java.time.ZonedDateTime;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

@Slf4j
public class JSONUtil {
    protected static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    protected JSONUtil() {
        // Bunch of static methods so there should be no need to create object of this class
        throw new UnsupportedOperationException("Invalid invocation of constructor");
    }

    public static Set<String> getAllKeys(ObjectNode node) {
        return getAllKeys(node, new HashSet<>());
    }

    private static Set<String> getAllKeys(ObjectNode node, Set<String> keys) {
        node.fields().forEachRemaining(entry -> keys.add(entry.getKey()));
        return keys;
    }

    public static <T> T deserializeJson(String body, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(body, clazz);
        } catch (Exception exception) {
            log.error(ExceptionMessages.ERROR_WHILE_DESERIALIZING_JSON_STRING);
            throw new MarshallingException(exception.getMessage());
        }
    }

    public static String asString(Object ob) {
        try {
            return OBJECT_MAPPER.writeValueAsString(ob);
        } catch (JsonProcessingException e) {
            throw new MarshallingException(e);
        }
    }

    public static JSONObject asJSONObject(Map<String, Object> map) {
        try {
            return new JSONObject(OBJECT_MAPPER.writeValueAsString(map));
        } catch (JsonProcessingException e) {
            throw new MarshallingException(e);
        }
    }

    public static ZonedDateTime getCreationDate(JsonNode captureJSONDocument) {
        return ZonedDateTime.parse(captureJSONDocument.get("creationDate").asText());
    }
}
