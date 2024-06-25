package com.rassafel.io.storage.aws.support;

import lombok.Data;
import org.springframework.util.unit.DataSize;

@Data
public class S3BlobStorageProperties {
    protected String bucket;
    protected String accessKey;
    protected String secretAccessKey;
    protected String region;
    protected String endpointUrl;
    private DataSize chunkSize = DataSize.ofKilobytes(8);
}
