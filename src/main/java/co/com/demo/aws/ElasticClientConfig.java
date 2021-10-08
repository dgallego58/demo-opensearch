package co.com.demo.aws;


import co.com.demo.rest.serializer.JacksonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.services.elasticsearch.ElasticsearchClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public enum ElasticClientConfig {

    ENVIRONMENT_ELASTICSEARCH {
        @Override
        public ElasticsearchClient getClient() {
            return ElasticsearchClient.builder()
                    .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                    .build();
        }
    },
    SYSTEM_PROPERTY_ELASTICSEARCH {
        private static final String ELASTIC_PROPERTIES = "aws-elastic-config.properties";

        @Override
        public ElasticsearchClient getClient() {
            try (InputStream is = ElasticClientConfig.class.getClassLoader().getResourceAsStream(ELASTIC_PROPERTIES)) {
                Properties props = new Properties();
                if (is == null) {
                    log.error("cannot be interpreted because props is null");
                    throw new IOException("Cannot be read from " + ELASTIC_PROPERTIES);
                }
                props.load(is);
                String propsAsJson = JacksonProvider.stringify.apply(props);
                log.info("Load accessKey: {}", propsAsJson);
                return ElasticsearchClient.builder()
                        .credentialsProvider(SystemPropertyCredentialsProvider.create())
                        .build();
            } catch (IOException e) {
                log.error("Cannot be read the property ", e);
                return ElasticClientConfig.ENVIRONMENT_ELASTICSEARCH.getClient();
            }
        }
    };

    private static final Logger log = LoggerFactory.getLogger(ElasticClientConfig.class);

    public abstract ElasticsearchClient getClient();

}
