package co.com.demo.rest.service;

import co.com.demo.rest.config.RestClientFactory;
import co.com.demo.rest.serializer.JacksonProvider;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RestService {

    private final RestClientFactory factory;

    public RestService(RestClientFactory factory) {
        this.factory = factory;
    }

    public static RestService create() {
        return new RestService(RestClientFactory.factory().withOutSslContext());
    }

    @SuppressWarnings({"unchecked"})
    public <T> Map<String, Object> indexItem(T body) {
        var httpRequest = HttpRequest.newBuilder(URI.create("https://localhost:9200/companies/_doc/1"))
                .PUT(HttpRequest.BodyPublishers.ofString(JacksonProvider.stringify.apply(body), StandardCharsets.UTF_8))
                .header("Authorization", factory.basicAuthHeader("admin", "admin"))
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .build();

        var responseBody = factory.build()
                .sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(HttpResponse::body)
                .join();

        return JacksonProvider.readAs(Map.class).apply(responseBody);
    }


}
