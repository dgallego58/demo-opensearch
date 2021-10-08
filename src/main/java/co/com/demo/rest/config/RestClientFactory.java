package co.com.demo.rest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;

public class RestClientFactory {

    private static final Duration TIME_OUT_CONNECTION = Duration.ofSeconds(3L);
    private static final Logger log = LoggerFactory.getLogger(RestClientFactory.class);

    private static final String TLS = "TLS";
    private final HttpClient.Builder httpClientBuilder;

    public RestClientFactory(HttpClient.Builder builder) {
        this.httpClientBuilder = builder;
    }

    public static RestClientFactory factory() {
        return new RestClientFactory(HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(TIME_OUT_CONNECTION));
    }

    public HttpClient.Builder builder() {
        return httpClientBuilder;
    }

    public HttpClient build() {
        return this.httpClientBuilder.build();
    }

    public String basicAuthHeader(String username, String password) {
        return "Basic " + Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    public RestClientFactory withOutSslContext() {
        TrustManager[] trustCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        //empty and disabled
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        //empty and disabled
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        try {
            var sslCtx = SSLContext.getInstance(TLS);
            sslCtx.init(null, trustCerts, new SecureRandom());
            httpClientBuilder.sslContext(sslCtx);
        } catch (NoSuchAlgorithmException e) {
            log.error("There is no such instance for the algorithm ", e);
        } catch (KeyManagementException e) {
            log.error("Cannot be initiated SSL context ", e);
        }

        return this;
    }

}
