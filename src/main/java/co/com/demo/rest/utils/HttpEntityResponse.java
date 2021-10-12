package co.com.demo.rest.utils;

import co.com.demo.rest.serializer.JacksonProvider;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class HttpEntityResponse {
    private final HttpResponse<String> httpResponse;

    public HttpEntityResponse(HttpResponse<String> httpResponse) {
        this.httpResponse = httpResponse;
    }

    public static HttpEntityResponse create(HttpResponse<String> httpResponse) {
        return new HttpEntityResponse(httpResponse);
    }

    public String serialize() {
        var body = JacksonProvider.tree().apply(httpResponse.body());
        var headers = httpResponse.headers().map();
        var statusCode = httpResponse.statusCode();
        var entityResponse = new EntityResponse(headers, statusCode, body);
        return JacksonProvider.stringify.apply(entityResponse);
    }


    public static class EntityResponse {
        private final Map<String, List<String>> headers;
        private final int statusCode;
        private final JsonNode body;

        public EntityResponse(Map<String, List<String>> headers, int statusCode, JsonNode body) {
            this.headers = headers;
            this.statusCode = statusCode;
            this.body = body;
        }

        public Map<String, List<String>> getHeaders() {
            return headers;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public JsonNode getBody() {
            return body;
        }
    }
}
