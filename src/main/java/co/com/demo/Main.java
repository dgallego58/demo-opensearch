package co.com.demo;

import co.com.demo.aws.ElasticClientConfig;

public class Main {

    public static void main(String[] args) {
        ElasticClientConfig.SYSTEM_PROPERTY_ELASTICSEARCH.getClient();
        DemoSender demoSender = new DemoSender();
        demoSender.execute();
    }
}
