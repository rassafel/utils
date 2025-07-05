/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.rassafel.blobstorage.aws.support;

import lombok.Data;
import org.springframework.util.unit.DataSize;

/**
 * Properties for configuring an S3 blob storage service.
 */
@Data
public class S3BlobStorageProperties {
    /**
     * The name of the S3 bucket where the blobs will be stored.
     */
    private String bucket;

    /**
     * The AWS credentials used to authenticate with the S3 service.
     */
    private String accessKey;

    /**
     * The AWS credentials used to authenticate with the S3 service.
     */
    private String secretAccessKey;

    /**
     * The region where the S3 bucket is located.
     */
    private String region;

    /**
     * The URL of the S3 endpoint to use. If not specified, AWS SDK will use the default endpoint for the region.
     */
    private String endpointUrl;

    /**
     * The maximum size of a single upload in bytes.
     */
    private DataSize chunkSize = DataSize.ofKilobytes(8);
}
