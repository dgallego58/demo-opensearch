package co.com.demo.aws;


import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.services.elasticsearch.ElasticsearchClient;

public enum ElasticClientConfig {

    ENVIRONMENT_ELASTICSEARCH_CONFIG {
        @Override
        public ElasticsearchClient getClient() {
            return ElasticsearchClient.builder()
                    .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                    .build();
        }
    };

    public abstract ElasticsearchClient getClient();

}
