package co.com.demo.aws.signature;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.http.HttpExecuteRequest;
import software.amazon.awssdk.http.HttpExecuteResponse;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.StringInputStream;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ElasticSearch implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(ElasticSearch.class);
    private final Aws4SignerParams params = Aws4SignerParams.builder()
            .awsCredentials(DefaultCredentialsProvider.create().resolveCredentials())
            .signingName("es")  // "es" stands for elastic search.  Change this to match your service!
            .signingRegion(Region.US_EAST_1)
            .build();
    private final Aws4Signer signer = Aws4Signer.create();
    private final SdkHttpClient httpClient = ApacheHttpClient.builder().build();

    /**
     * @param path should not have a leading "/"
     */
    private HttpExecuteResponse restRequest(SdkHttpMethod method, String path) throws IOException {
        return restRequest(method, path, null);
    }

    private HttpExecuteResponse restRequest(SdkHttpMethod method, String path, String body)
            throws IOException {
        SdkHttpFullRequest.Builder b = SdkHttpFullRequest.builder()
                .encodedPath(path)
                .host("vpc-demo-opensearch-gkcj5itfvb6ogv7pbp2wpj4oqa.us-east-1.es.amazonaws.com")
                .method(method)
                .protocol("https");
        if (body != null) {
            b.putHeader("Content-Type", "application/json; charset=utf-8");
            b.contentStreamProvider(() -> new StringInputStream(body));
        }
        SdkHttpFullRequest request = b.build();

        // now sign it
        SdkHttpFullRequest signedRequest = signer.sign(request, params);
        HttpExecuteRequest.Builder rb = HttpExecuteRequest.builder().request(signedRequest);
        // !!!: line below is necessary even though the contentStreamProvider is in the request.
        // Otherwise the body will be missing from the request and auth signature will fail.
        request.contentStreamProvider().ifPresent(rb::contentStreamProvider);

        return httpClient.prepareRequest(rb.build()).call();
    }

    public void search(String indexName, String searchString) throws IOException {
        JsonMapper jsonBuilder = JsonMapper.builder().build();
        var rootNode = jsonBuilder.createObjectNode();
        var queryNode = jsonBuilder.createObjectNode();
        var matchNode = jsonBuilder.createObjectNode();
        var queryLeafNode = jsonBuilder.createObjectNode();

        queryLeafNode.put("query", searchString);
        matchNode.set("name", queryLeafNode);
        queryNode.set("query", matchNode);
        rootNode.set("query", queryNode);
        HttpExecuteResponse result = restRequest(SdkHttpMethod.GET, indexName + "/_search",
                rootNode.toString());
        log.info("Search results:");
        String jsonResp = jsonBuilder.writer().writeValueAsString(result.responseBody());
        log.info(jsonResp);
    }

    /**
     * @return success status
     */
    public boolean createIndex(String indexName) throws IOException {
        if (indexName.contains("/")) {
            throw new IllegalArgumentException("indexName cannot contain '/' character");
        }
        HttpExecuteResponse r = restRequest(SdkHttpMethod.PUT, indexName);
        String requestPath = String.format("PUT /%s response code: %d", indexName, r.httpResponse().statusCode());
        log.info(requestPath);
        printInputStream(r.responseBody().get());
        return r.httpResponse().isSuccessful();
    }

    private void printInputStream(InputStream is) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String readLine;
            while (((readLine = br.readLine()) != null)) log.info(readLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean postDoc(String indexName, String docId, String docBody) throws IOException {
        HttpExecuteResponse response = restRequest(
                SdkHttpMethod.PUT,
                String.format("%s/_doc/%s", indexName, docId),
                docBody
        );
        log.info("Index operation response:");
        printInputStream(response.responseBody().get());
        return response.httpResponse().isSuccessful();
    }

    @Override
    public void close() {
        httpClient.close();
    }
}

