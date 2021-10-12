package co.com.demo;

import co.com.demo.aws.ElasticClientConfig;
import co.com.demo.rest.contracts.ElasticIndexes;
import co.com.demo.rest.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class DemoIndex {

    private static final Logger log = LoggerFactory.getLogger(DemoIndex.class);

    public void execute() {
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
    }


}
