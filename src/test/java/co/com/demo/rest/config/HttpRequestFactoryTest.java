package co.com.demo.rest.config;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

class HttpRequestFactoryTest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequestFactoryTest.class);


    @Test
    void testHeadersAreNotEmpty() {
        var factory = HttpRequestFactory.defaultBuilder();
        List<String> values = Arrays.asList("a", "b", "c");
        List<String> values2 = Arrays.asList("1", "2", "3");
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("letter", values);
        headers.put("number", values2);
        var builder = factory.withHeaders(headers).withUri("https://localhost:9000");
        var jsonHeaders = builder.requestBuilder().build().headers().map();
        assertFalse(jsonHeaders.isEmpty());
        log.info("Headers {}", jsonHeaders);
    }

}
