package co.com.demo;

import co.com.demo.aws.ClientAutoWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
/*        ElasticClientConfig.SYSTEM_PROPERTY_ELASTICSEARCH.getClient();

        DemoElastic demoElastic = new DemoElastic();

        demoElastic.indexing();
        demoElastic.dataSender();

        try (ElasticSearch elasticSearch = new ElasticSearch()) {
            elasticSearch.createIndex("companies");

            var doc = new HashMap<>();
            doc.put("demo", "company");
            elasticSearch.postDoc("companies", "2", JacksonProvider.STRINGIFY.apply(doc));
        }*/

        ClientAutoWrapper clientAutoWrapper = new ClientAutoWrapper();
        Map<String, Object> doc = new HashMap<>();
        doc.put("demo", "company");
        clientAutoWrapper.request("companies", doc);

    }
}
