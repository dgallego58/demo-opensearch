package co.com.demo.rest.service;

import co.com.demo.rest.config.RestClientFactory;
import co.com.demo.rest.serializer.JacksonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static co.com.demo.rest.service.HttpUtils.ACCEPT;
import static co.com.demo.rest.service.HttpUtils.APPLICATION_JSON_VALUE;
import static co.com.demo.rest.service.HttpUtils.AUTHORIZATION;
import static co.com.demo.rest.service.HttpUtils.CONTENT_TYPE;
import static co.com.demo.rest.service.HttpUtils.HOST;

@SuppressWarnings({"unchecked"})
public class RestService {

    private static final String ADMIN = "admin";
    private static final Logger log = LoggerFactory.getLogger(RestService.class);
    private final RestClientFactory factory;

    public RestService(RestClientFactory factory) {
        this.factory = factory;
    }

    public static RestService create() {
        return new RestService(RestClientFactory.factory().withOutSslContext());
    }

    public <T> Map<String, Object> indexItem(T body) {
        var httpRequest = HttpRequest.newBuilder(URI.create(HOST + "/companies/_doc/" + UUID.randomUUID()))
                .PUT(HttpRequest.BodyPublishers.ofString(JacksonProvider.stringify.apply(body), StandardCharsets.UTF_8))
                .header(AUTHORIZATION, factory.basicAuthHeader(ADMIN, ADMIN))
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .build();

        var responseBody = factory.build()
                .sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(HttpResponse::body)
                .join();

        return JacksonProvider.readAs(Map.class).apply(responseBody);
    }

    public Map<String, Object> analyze(String tokens) {
        var dictionary = new HashMap<String, String>();
        dictionary.put("text", tokens);
        var jsonRequestBody = JacksonProvider.stringify.apply(dictionary);
        var request = HttpRequest.newBuilder(URI.create(HOST + "/_analyze"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .header(AUTHORIZATION, factory.basicAuthHeader(ADMIN, ADMIN))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .build();
        var responseBody = factory.build()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(HttpResponse::body)
                .join();
        return JacksonProvider.readAs(Map.class).apply(responseBody);
    }

    public Map<String, Object> createDocument(String name) {
        String fixedName = name.trim();
        var req = HttpRequest.newBuilder(URI.create(HOST + "/" + fixedName))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .header(AUTHORIZATION, factory.basicAuthHeader(ADMIN, ADMIN))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .build();
        var resp = factory.build()
                .sendAsync(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .join();
        var body = resp.body();
        var statusCode = resp.statusCode();
        log.warn("Checking out status code for createDocument {}", statusCode);
        return JacksonProvider.readAs(Map.class).apply(body);
    }

    public Map<String, Object> searchIndexes(String indexName) {
        var req = HttpRequest.newBuilder(URI.create(HOST + "/" + indexName + "/_search"))
                .GET()
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, factory.basicAuthHeader(ADMIN, ADMIN))
                .build();
        var resp = factory.build()
                .sendAsync(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(HttpResponse::body)
                .join();
        return JacksonProvider.readAs(Map.class).apply(resp);
    }

    public Map<String, Object> listAllIndexes() {
        var req = HttpRequest.newBuilder(URI.create(HOST + "/_all"))
                .GET()
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, factory.basicAuthHeader(ADMIN, ADMIN))
                .build();
        var resp = factory.build()
                .sendAsync(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(HttpResponse::body)
                .join();
        return JacksonProvider.readAs(Map.class).apply(resp);
    }

}
