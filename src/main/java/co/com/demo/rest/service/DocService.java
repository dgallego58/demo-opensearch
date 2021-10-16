package co.com.demo.rest.service;

import co.com.demo.rest.config.HttpRequestFactory;
import co.com.demo.rest.config.RestClientFactory;
import co.com.demo.rest.contracts.ElasticDocs;
import co.com.demo.rest.serializer.JacksonProvider;
import co.com.demo.rest.utils.HttpEntityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;

import static co.com.demo.rest.utils.HttpUtils.HOST;

public class DocService implements ElasticDocs {

    public static final String INDEX_NAME_DOC_ID = "/{INDEX_NAME}/_doc{/ID}";
    public static final String ID_ROUTE = "{/ID}";
    public static final String INDEX_NAME = "{INDEX_NAME}";
    private static final Logger log = LoggerFactory.getLogger(DocService.class);
    private final HttpClient httpClient;
    private final HttpRequestFactory factory;
    private final String indexName;


    private DocService(HttpClient httpClient, HttpRequestFactory factory, String indexName) {
        this.httpClient = httpClient;
        this.factory = factory;
        this.indexName = indexName;
    }

    public static DocService defaultDocService(String indexName) {
        if (indexName == null) {
            throw new IllegalArgumentException("The index should not be null");
        }
        return new DocService(RestClientFactory.factory().withOutSslContext()
                .build(), HttpRequestFactory.defaultBuilder().withBasic("admin", "admin"), indexName);
    }

    public ElasticDocs.Create create() {
        return (objectRequest, id) -> {
            var flatUri = uriWithIndex();
            if (id != null) {
                flatUri = flatUri.replace(ID_ROUTE, "/" + id);
            } else {
                flatUri = flatUri.replace(ID_ROUTE, "");
            }
            log.info("URI {}", flatUri);
            var resp = factory.post(JacksonProvider.STRINGIFY.apply(objectRequest))
                    .withUri(flatUri)
                    .sendWith(httpClient);
            return HttpEntityResponse.create(resp).serialize();
        };
    }

    public ElasticDocs.Get getDoc() {
        return (id, sourceParams) -> {
            var flatUri = uriWithIndex().replace(ID_ROUTE, "/" + id);

            if (sourceParams.length > 0) {
                var sourceParameters = "?_source=" + String.join(",", sourceParams);
                flatUri = flatUri + sourceParameters;
            }
            log.info("Send with {}", flatUri);
            var resp = factory.get().withUri(flatUri).sendWith(httpClient);
            return HttpEntityResponse.create(resp).serialize();
        };
    }

    public ElasticDocs.Checks exists() {
        return docId -> {
            var flatUri = uriWithIndex().replace(ID_ROUTE, docId);
            var response = factory.head().withUri(flatUri).sendWith(httpClient);
            return response.statusCode() / 100 == 2;
        };
    }

    public ElasticDocs.Update update() {

        return (docId, doc) -> {
            var flatUri = uriWithIndex().replace(ID_ROUTE, docId);
            String json = JacksonProvider.STRINGIFY.apply(doc);
            var response = factory.post(json).withUri(flatUri).sendWith(httpClient);
            return HttpEntityResponse.create(response).serialize();
        };
    }

    public ElasticDocs.Delete deleteById() {
        return id -> {
            var flatUri = uriWithIndex().replace(ID_ROUTE, id);
            var resp = factory.delete().withUri(flatUri).sendWith(httpClient);
            return resp.statusCode() / 100 == 2;
        };
    }

    private String uriWithIndex() {
        return HOST + INDEX_NAME_DOC_ID.replace(INDEX_NAME, indexName);
    }

}
