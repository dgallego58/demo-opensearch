package co.com.demo.rest.serializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class JacksonProvider {

    public static final ObjectMapper jsonObjectMapper = JsonMapper.builder()
            .addModules(new JavaTimeModule(), new Jdk8Module(), new ParameterNamesModule())
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    private static final Logger log = LoggerFactory.getLogger(JacksonProvider.class);
    public static final Function<Object, String> STRINGIFY = object -> {
        try {
            return jsonObjectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Cannot be serialized ", e);
            return "";
        }
    };

    private JacksonProvider() {
        //private
    }

    /**
     * Use it as:
     * <pre>{@code
     *  var map = JacksonProvider.readAs(Map.class).apply(jsonString);
     * }</pre>
     *
     * @param type the class to be converted from the json
     * @param <T>  type of class
     * @return an object or null if json is invalid or class is not concrete
     */
    public static <T> Function<String, T> readAs(Class<T> type) {
        return json -> {
            try {
                return jsonObjectMapper.readValue(json.getBytes(StandardCharsets.UTF_8), type);
            } catch (IOException e) {
                log.error("Cannot be processed/deserialized", e);
                return null;
            }
        };
    }

    public static Function<String, JsonNode> tree() {
        return json -> {
            try {
                return jsonObjectMapper.readTree(json);
            } catch (JsonProcessingException e) {
                log.error("Cannot be extract json node", e);
                return null;
            }
        };
    }

}

