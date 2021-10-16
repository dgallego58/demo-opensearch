package co.com.demo.rest.config;

import co.com.demo.rest.serializer.JacksonProvider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static co.com.demo.rest.utils.HttpUtils.ACCEPT;
import static co.com.demo.rest.utils.HttpUtils.APPLICATION_JSON_VALUE;
import static co.com.demo.rest.utils.HttpUtils.AUTHORIZATION;
import static co.com.demo.rest.utils.HttpUtils.CONTENT_TYPE;

public class HttpRequestFactory {

    private final HttpRequest.Builder httpRequestBuilder;

    private HttpRequestFactory() {
        this.httpRequestBuilder = HttpRequest.newBuilder()
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .timeout(Duration.ofSeconds(3L));
    }

    public static HttpRequestFactory defaultBuilder() {
        return new HttpRequestFactory();
    }

    public HttpRequest.Builder requestBuilder() {
        return httpRequestBuilder;
    }

    public HttpRequestFactory withBasic(String username, String password) {
        byte[] encoded = (username + ":" + password).getBytes(StandardCharsets.UTF_8);
        var header = "Basic " + Base64.getEncoder().encodeToString(encoded);
        httpRequestBuilder.header(AUTHORIZATION, header);
        return this;
    }

    public HttpRequestFactory get() {
        httpRequestBuilder.GET();
        return this;
    }

    public HttpRequestFactory post(Object requestBody) {
        var jsonRequestBody = JacksonProvider.STRINGIFY.apply(requestBody);
        if (jsonRequestBody.isEmpty()) {
            httpRequestBuilder.POST(HttpRequest.BodyPublishers.noBody());
            return this;
        }
        httpRequestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody));
        return this;
    }

    public HttpRequestFactory put(Object requestBody) {
        var jsonRequestBody = JacksonProvider.STRINGIFY.apply(requestBody);
        if (jsonRequestBody.isEmpty()) {
            httpRequestBuilder.PUT(HttpRequest.BodyPublishers.ofString(jsonRequestBody));
            return this;
        }
        httpRequestBuilder.PUT(HttpRequest.BodyPublishers.noBody());
        return this;
    }

    public HttpRequestFactory delete() {
        httpRequestBuilder.DELETE();
        return this;
    }

    public HttpRequestFactory head() {
        httpRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
        return this;
    }

    public HttpRequestFactory withUri(String url) {
        var uri = URI.create(url);
        httpRequestBuilder.uri(uri);
        return this;
    }

    public HttpRequestFactory withHeaders(Map<String, List<String>> headers) {
        String comma = ",";
        var arrayHeaders = headers.keySet()
                .stream()
                .map(headerKey -> headers.get(headerKey)
                        .stream()
                        .map(headerValue -> headerKey + comma + headerValue)
                        .collect(Collectors.joining(comma)))
                .collect(Collectors.joining(comma));
        httpRequestBuilder.headers(arrayHeaders.split(comma));
        return this;
    }

    public HttpResponse<String> sendWith(HttpClient httpClient) {
        return httpClient.sendAsync(httpRequestBuilder.build(), HttpResponse.BodyHandlers.ofString()).join();
    }

}
