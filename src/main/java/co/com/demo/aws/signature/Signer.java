package co.com.demo.aws.signature;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.regions.Region;

public class Signer {

    public AWSRequestSigningInterceptor interceptor() {
        try (var credentials = DefaultCredentialsProvider.create()) {
            Aws4Signer aws4Signer = Aws4Signer.create();
            var params = Aws4SignerParams.builder()
                    .awsCredentials(credentials.resolveCredentials())
                    .signingName("es")
                    .signingRegion(Region.US_EAST_1)
                    .build();
            return new AWSRequestSigningInterceptor(aws4Signer, params);
        }
    }
}
