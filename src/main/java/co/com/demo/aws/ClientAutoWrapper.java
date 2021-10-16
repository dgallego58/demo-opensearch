package co.com.demo.aws;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class ClientAutoWrapper {

    private static final Logger log = LoggerFactory.getLogger(ClientAutoWrapper.class);

    public void request(String indexName, Map<String, Object> doc) throws IOException {

        //Create a client.
        RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "https"))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        //.addInterceptorFirst(interceptor)
                        .setSSLHostnameVerifier((hostname, session) -> true));
        try (RestHighLevelClient hlClient = new RestHighLevelClient(builder)) {

            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);

            var createIndexResp = hlClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            log.info("Create index resp {}", createIndexResp);

            IndexRequest indexRequest = new IndexRequest(createIndexResp.index())
                    .id(String.valueOf(doc.get("id")))
                    .source(doc);
            var response = hlClient.index(indexRequest, RequestOptions.DEFAULT);
            var json = response.toString();
            log.info("response is {}", json);

            var deleteIndexResp = hlClient.indices().delete(new DeleteIndexRequest(indexName), RequestOptions.DEFAULT);
            log.info("Delete index resp {}", deleteIndexResp.isAcknowledged());
        }
    }

}
