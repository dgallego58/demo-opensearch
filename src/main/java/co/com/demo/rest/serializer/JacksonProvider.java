package co.com.demo.rest.serializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class JacksonProvider {

    public static final ObjectMapper jsonObjectMapper = JsonMapper.builder()
            .addModules(new JavaTimeModule(), new Jdk8Module(), new ParameterNamesModule())
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();
    private static final Logger log = LoggerFactory.getLogger(JacksonProvider.class);
    public static final Function<Object, String> stringify = object -> {
        try {
            return jsonObjectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Cannot be serialized ", e);
            return "Error";
        }
    };

    private JacksonProvider() {
        //private
    }

    public static <T> Function<String, T> readAs(Class<T> type) {
        return json -> {
            try {
                return jsonObjectMapper.readValue(json, type);
            } catch (JsonProcessingException e) {
                log.error("Cannot be processed/deserialized ", e);
                return null;
            }
        };

    }
}
