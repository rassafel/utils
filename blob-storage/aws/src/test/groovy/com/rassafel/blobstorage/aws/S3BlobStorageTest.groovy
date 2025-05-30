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

package com.rassafel.blobstorage.aws

import org.springframework.util.unit.DataSize
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Stepwise

import com.rassafel.blobstorage.BlobStorageSpecification
import com.rassafel.blobstorage.aws.support.DefaultS3ClientProvider
import com.rassafel.blobstorage.core.BlobStorage

@Stepwise
@IgnoreIf(value = {
    !System.getenv("S3_ENABLED")
}, reason = "S3 not provided")
class S3BlobStorageTest extends BlobStorageSpecification {

//    @Shared
//    def clientProvider = new DefaultS3ClientProvider("p2FNEsccm1MwPWLCgntF",
//        "sJXQ7Tz96JiqZvkAki7Nh0QW0uSKZtSa8AlsgROj",
//        "ru-samara",
//        "http://localhost:9000")

    @Shared
    def clientProvider = new DefaultS3ClientProvider(
            System.getenv("S3_ACCESS_KEY"),
            System.getenv("S3_SECRET_ACCESS_KEY"),
            System.getenv("S3_REGION"),
            System.getenv("S3_URL"),
    )

    @Shared
    def storage = new S3BlobStorage(DataSize.ofMegabytes(8), "s3-test",
            clientProvider, clock, keyGen)

    @Override
    BlobStorage storage() {
        return storage
    }
}
