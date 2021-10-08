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
        MDC.put("key", "Calling Service");
        domains.domainNames().forEach(domainInfo -> log.info(domainInfo.domainName()));
        var item = new HashMap<String, Object>();
        item.put("company", "demo-company");
        var result = RestService.create().indexItem(item);
        log.info("Result is {}", result);
        MDC.clear();
    }


}
