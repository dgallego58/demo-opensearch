package co.com.demo;

import co.com.demo.aws.ElasticClientConfig;
import co.com.demo.rest.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;

public class DemoSender {

    private static final Logger log = LoggerFactory.getLogger(DemoSender.class);

    public void execute() {
        var client = ElasticClientConfig.ENVIRONMENT_ELASTICSEARCH.getClient();
        var domains = client.listDomainNames();
        var elasticService = RestService.create();

        MDC.put("key", "Domain Names");
        domains.domainNames().forEach(domainInfo -> log.info("Domains from elastic {}", domainInfo.domainName()));

        MDC.put("key", "Index Item");
        var item = new HashMap<String, Object>();
        item.put("company", "demo-company");
        log.info("Result is {}", elasticService.indexItem(item));

        MDC.put("key", "Analyze");
        var analyze = elasticService.analyze("palabras que hacen match con companies");
        log.info("Analyze result {}", analyze);

        MDC.put("key", "Document");
        var documentResult = elasticService.createDocument("interests");
        log.info("Document result  {}", documentResult);

        MDC.put("key", "Index");
        var singleIndex = elasticService.searchIndexes("companies");
        log.info("Single Index result  {}", singleIndex);

        MDC.put("key", "Indexes");
        var indexes = elasticService.listAllIndexes();
        log.info("Indexes are {}", indexes);

        MDC.clear();
    }


}
