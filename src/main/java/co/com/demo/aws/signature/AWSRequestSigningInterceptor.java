package co.com.demo.aws.signature;


import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.http.protocol.HttpCoreContext.HTTP_TARGET_HOST;

/**
 * AWS SDK v2 version of: https://github.com/awslabs/aws-request-signing-apache-interceptor/blob/master/src/main/java/com/amazonaws/http/AWSRequestSigningApacheInterceptor.java
 */

public class AWSRequestSigningInterceptor implements HttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AWSRequestSigningInterceptor.class);
    private final Aws4Signer signer;
    private final Aws4SignerParams params;

    public AWSRequestSigningInterceptor(Aws4Signer signer, Aws4SignerParams params) {
        this.signer = signer;
        this.params = params;
    }

    /**
     * @param params list of HTTP query params as NameValuePairs
     * @return a Multimap of HTTP query params
     */
    private static Map<String, String> nvpToMapParams(final List<NameValuePair> params) {
        Map<String, String> parameterMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (NameValuePair nvp : params) {
            parameterMap.putIfAbsent(nvp.getName(), nvp.getValue());
        }
        return parameterMap;
    }

    /**
     * @param headers modeled Header objects
     * @return a Map of header entries
     */
    private static Map<String, String> headerArrayToMap(final Header[] headers) {
        Map<String, String> headersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Header header : headers) {
            if (!skipHeader(header)) {
                headersMap.put(header.getName(), header.getValue());
            }
        }
        return headersMap;
    }

    /**
     * @param header header line to check
     * @return true if the given header should be excluded when signing
     */
    private static boolean skipHeader(final Header header) {
        return ("content-length".equalsIgnoreCase(header.getName())
                && "0".equals(header.getValue())) // Strip Content-Length: 0
                || "host".equalsIgnoreCase(header.getName()); // Host comes from endpoint
    }

    /**
     * @param mapHeaders Map of header entries
     * @return modeled Header objects
     */
    private static Header[] mapToHeaderArray(final Map<String, List<String>> mapHeaders) {
        Header[] headers = new Header[mapHeaders.size()];
        int i = 0;
        for (Map.Entry<String, List<String>> headerEntry : mapHeaders.entrySet()) {
            for (String value : headerEntry.getValue()) {
                headers[i++] = new BasicHeader(headerEntry.getKey(), value);
            }
        }
        return headers;
    }

    @Override
    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(request.getRequestLine().getUri());
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URI", e);
        }

        final SdkHttpFullRequest.Builder signableRequestBuilder = SdkHttpFullRequest.builder();

        final HttpHost host = (HttpHost) context.getAttribute(HTTP_TARGET_HOST);
        if (host != null) {
            var uri = URI.create(host.toURI());
            log.info("URI is {}", uri);
            signableRequestBuilder.uri(uri);
        }
        final SdkHttpMethod httpMethod = SdkHttpMethod.fromValue(request.getRequestLine().getMethod());
        signableRequestBuilder.method(httpMethod);
        try {
            var rawPath = uriBuilder.build().getRawPath();
            log.info("Raw Path {}", rawPath);
            var s = signableRequestBuilder.encodedPath(rawPath);
            var protocol = s.protocol();
            var hostFixed = s.host();
            var encodedPath = s.encodedPath();
            log.info("Encoded Protocol: {} Host: {}, path: {}, port: {}", protocol, hostFixed, encodedPath, s.port());

        } catch (URISyntaxException e) {
            throw new IOException("Invalid URI", e);
        }

        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) request;

            if (httpEntityEnclosingRequest.getEntity() != null) {
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                httpEntityEnclosingRequest.getEntity().writeTo(outputStream);
                signableRequestBuilder.contentStreamProvider(() -> new ByteArrayInputStream(outputStream.toByteArray()));
            }
        }

        // Append Parameters and Headers
        nvpToMapParams(uriBuilder.getQueryParams()).forEach(signableRequestBuilder::appendRawQueryParameter);
        headerArrayToMap(request.getAllHeaders()).forEach(signableRequestBuilder::appendHeader);

        // Sign it
        final SdkHttpFullRequest signedRequest = signer.sign(signableRequestBuilder.build(), params);
        log.info("Signed Request {}", signedRequest.getUri());
        // Now copy everything back
        request.setHeaders(mapToHeaderArray(signedRequest.headers()));
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest httpEntityEnclosingRequest =
                    (HttpEntityEnclosingRequest) request;
            if (httpEntityEnclosingRequest.getEntity() != null) {
                BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
                if (signedRequest.contentStreamProvider().isPresent()) {
                    basicHttpEntity.setContent(signedRequest.contentStreamProvider().get().newStream());
                } else {
                    throw new IllegalStateException("Empty content stream was not expected!");
                }
                httpEntityEnclosingRequest.setEntity(basicHttpEntity);
            }
        }
    }

}
