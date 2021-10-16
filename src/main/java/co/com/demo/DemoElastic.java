package co.com.demo;

import co.com.demo.aws.ElasticClientConfig;
import co.com.demo.rest.contracts.ElasticIndexes;
import co.com.demo.rest.service.DocService;
import co.com.demo.rest.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class DemoElastic {

    private static final Logger log = LoggerFactory.getLogger(DemoElastic.class);

    public DemoElastic indexing() {
        MDC.put("key", "Domain Names");
        var client = ElasticClientConfig.ENVIRONMENT_ELASTICSEARCH.getClient();
        var domains = client.listDomainNames();
        domains.domainNames().forEach(domainInfo -> log.info("Domains from elastic {}", domainInfo.domainName()));

        ElasticIndexes indexService = IndexService.createDefault();
        MDC.put("key", "Index Services");
        var indexName = "companies";
        var createdIndexResp = indexService.create().apply(indexName);
        log.info("Create index :{}", createdIndexResp);
        log.info("Exist index :{}", indexService.existsIndex(indexName, value -> value / 100 == 2));
        log.info("All Result :{}", indexService.all().get());
        var getIdxResp = indexService.getIndex().apply(indexName);
        log.info("Get index called companies {}", getIdxResp);
        var deleteIdxResp = indexService.delete().apply(indexName);
        log.info("Delete index {}", deleteIdxResp);
        MDC.clear();
        return this;
    }

    public DemoElastic dataSender() {
        MDC.put("key", "DOCS");
        String indexName = "companies";
        ElasticIndexes indexes = IndexService.createDefault();
        DocService docService = DocService.defaultDocService(indexName);

        var creationResult = indexes.create().apply(indexName);
        log.info("Creating the index: {}", creationResult);

        var myCustomObject = new HashMap<String, String>();
        myCustomObject.put("id", UUID.randomUUID().toString());
        myCustomObject.put("name", "indexerCompanyDemo");
        myCustomObject.put("randomCode", "TX-" + new Random().nextInt(10));
        //start process
        var docCreationResultWithId = docService.create().sendFor(myCustomObject, myCustomObject.get("id"));
        log.info("Creating doc over index {}, result is {}", indexName, docCreationResultWithId);

        var docCreationResult = docService.create().sendFor(myCustomObject, null);
        log.info("Creating doc over index {} without id, result is {}", indexes, docCreationResult);

        var getDocById = docService.getDoc().get(myCustomObject.get("id"), "name", "randomCode");
        log.info("Get Doc by id over index {}, with id {}, result is {}", indexName, myCustomObject.get("id"), getDocById);

        var getDocByPartialSourceId = docService.getDoc().get(myCustomObject.get("id"), "name", "randomCode");
        log.info("Get Doc by id over index {}, with id {}, result is {}", indexName, myCustomObject.get("id"), getDocByPartialSourceId);

        boolean idExists = docService.exists().exists(myCustomObject.get("id"));
        log.info("Head to check if a doc exists, result {}", idExists);

        var updateDoc = docService.update()
                .setDoc(myCustomObject.get("id"), myCustomObject.put("name", "updatedIndexer"));
        log.info("Head to check if a doc exists, result {}", updateDoc);
        log.info("Check result {}", docService.getDoc().get(myCustomObject.get("id")));

        boolean isDeleted = docService.deleteById().isDeleted(myCustomObject.get("id"));
        log.info("Delete doc by its id result {}", isDeleted);

        //end process
        var deletingResult = indexes.delete().apply(indexName);
        log.info("Cleaning up the index: {}", deletingResult);
        MDC.clear();
        return this;
    }


}
