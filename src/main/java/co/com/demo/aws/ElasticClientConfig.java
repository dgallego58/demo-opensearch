package co.com.demo.aws;


import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.services.elasticsearch.ElasticsearchClient;

public enum ElasticClientConfig {

    LOCAL_ELASTICSEARCH_CONFIG {
        @Override
        public ElasticsearchClient getClient() {
            System.setProperty("aws.accessKeyId", "expected");
            System.setProperty("aws.secretAccessKey", "ex");
            System.setProperty("aws.sessionToken", "ex");

            return ElasticsearchClient.builder()
                    .credentialsProvider(SystemPropertyCredentialsProvider.create())
                    .build();
        }
    },
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
