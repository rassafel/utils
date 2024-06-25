package com.rassafel.io.storage.aws.support;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.aws.S3ClientProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultS3ClientProvider implements S3ClientProvider {
    protected final AtomicReference<S3Client> s3ClientReference = new AtomicReference<>();
    protected final AtomicReference<S3AsyncClient> s3AsyncClientAtomicReference = new AtomicReference<>();
    private final Object clientMonitor = new Object();
    protected String accessKey;
    protected String secretAccessKey;
    protected String region;
    protected String endpointUrl;

    public DefaultS3ClientProvider(String accessKey, String secretAccessKey, String region, String endpointUrl) {
        this.accessKey = accessKey;
        this.secretAccessKey = secretAccessKey;
        this.region = region;
        this.endpointUrl = endpointUrl;
    }

    public void refreshS3Client() {
        var region = this.region;
        Assert.hasText(region, "region must not be empty");
        var credentialsProvider = getAwsCredentialsProvider();
        var endpointUrl = this.endpointUrl;
        var s3Client = createClient(S3Client.builder(), credentialsProvider, region, endpointUrl).build();
        var s3AsyncClient = createClient(S3AsyncClient.builder(), credentialsProvider, region, endpointUrl).build();
        s3ClientReference.set(s3Client);
        s3AsyncClientAtomicReference.set(s3AsyncClient);
    }

    protected AwsCredentialsProvider getAwsCredentialsProvider() {
        var accessKey = this.accessKey;
        var secretAccessKey = this.secretAccessKey;
        if (accessKey != null && secretAccessKey != null) {
            var awsCredentials = AwsBasicCredentials.create(accessKey, secretAccessKey);
            return StaticCredentialsProvider.create(awsCredentials);
        } else {
            return DefaultCredentialsProvider.builder().build();
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends AwsClientBuilder<?, ?>> T createClient(T builder,
                                                                AwsCredentialsProvider credentialsProvider,
                                                                String region,
                                                                @Nullable String endpointUrl) {
        if (StringUtils.isNotBlank(endpointUrl)) builder = (T) builder
            .endpointOverride(URI.create(endpointUrl));
        return (T) builder.region(Region.of(region))
            .credentialsProvider(credentialsProvider);
    }

    @Override
    public S3Client getS3Client() {
        var client = s3ClientReference.get();
        if (client == null) {
            synchronized (clientMonitor) {
                client = s3ClientReference.get();
                if (client != null) return client;
                refreshS3Client();
                client = s3ClientReference.get();
            }
        }
        return client;
    }

    @Override
    public S3AsyncClient getS3AsyncClient() {
        var client = s3AsyncClientAtomicReference.get();
        if (client == null) {
            synchronized (clientMonitor) {
                client = s3AsyncClientAtomicReference.get();
                if (client != null) return client;
                refreshS3Client();
                client = s3AsyncClientAtomicReference.get();
            }
        }
        return client;
    }
}
