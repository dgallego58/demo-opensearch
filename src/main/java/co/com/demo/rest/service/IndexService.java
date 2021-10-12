package co.com.demo.rest.service;

import co.com.demo.rest.config.HttpRequestFactory;
import co.com.demo.rest.config.RestClientFactory;
import co.com.demo.rest.contracts.ElasticIndexes;
import co.com.demo.rest.utils.HttpEntityResponse;

import java.net.http.HttpClient;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static co.com.demo.rest.utils.HttpUtils.HOST;

public class IndexService implements ElasticIndexes {

    private final HttpClient httpClient;
    private final HttpRequestFactory requestFactory;

    private IndexService(RestClientFactory clientFactory, HttpRequestFactory requestFactory) {
        //encapsulated
        this.httpClient = clientFactory.withOutSslContext().build();
        this.requestFactory = requestFactory.withBasic("admin", "admin");
    }

    public static ElasticIndexes createDefault() {
        return new IndexService(RestClientFactory.factory(), HttpRequestFactory.defaultBuilder());
    }

    @Override
    public Supplier<String> all() {
        var httpResponse = requestFactory.get().withUri(HOST + "/_all").sendWith(httpClient);
        return () -> HttpEntityResponse.create(httpResponse).serialize();
    }

    @Override
    public boolean existsIndex(String indexName, IntPredicate statusPredicate) {
        var statusCode = requestFactory.head().withUri(HOST + "/" + indexName)
                .sendWith(httpClient)
                .statusCode();
        return statusPredicate.test(statusCode);
    }

    @Override
    public UnaryOperator<String> create() {
        return indexName -> HttpEntityResponse.create(requestFactory.put(null)
                        .withUri(HOST + "/" + indexName)
                        .sendWith(httpClient))
                .serialize();
    }

    @Override
    public UnaryOperator<String> getIndex() {
        return indexName -> HttpEntityResponse.create(requestFactory.get()
                        .withUri(HOST + "/" + indexName)
                        .sendWith(httpClient))
                .serialize();
    }

    @Override
    public UnaryOperator<String> delete() {
        return indexName -> HttpEntityResponse.create(requestFactory.delete()
                        .withUri(HOST + "/" + indexName)
                        .sendWith(httpClient))
                .serialize();
    }

}
