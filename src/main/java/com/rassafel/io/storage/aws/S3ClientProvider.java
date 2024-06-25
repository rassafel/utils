package com.rassafel.io.storage.aws;

import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

public interface S3ClientProvider {
    S3Client getS3Client();

    S3AsyncClient getS3AsyncClient();
}
