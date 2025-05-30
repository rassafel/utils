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

package com.rassafel.blobstorage.aws;

import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Provides an instance of {@link S3Client} and {@link S3AsyncClient}.
 */
public interface S3ClientProvider {
    /**
     * Returns an instance of synchronous client.
     *
     * @return an instance of synchronous client.
     */
    S3Client getS3Client();

    /**
     * Returns an instance of asynchronous client.
     *
     * @return an instance of asynchronous client.
     */
    S3AsyncClient getS3AsyncClient();
}
