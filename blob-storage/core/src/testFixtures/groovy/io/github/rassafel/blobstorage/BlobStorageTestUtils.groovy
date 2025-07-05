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

package io.github.rassafel.blobstorage

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

import org.springframework.lang.Nullable

import io.github.rassafel.blobstorage.core.StoredBlobObject

class BlobStorageTestUtils {
    public static final Charset CHARSET = StandardCharsets.UTF_8

    static int getBytesSize(String value) {
        return value.getBytes(CHARSET).length
    }

    static ByteArrayInputStream toInputStream(String value) {
        return new ByteArrayInputStream(value.getBytes(CHARSET))
    }

    @Nullable
    static String blobToString(@Nullable StoredBlobObject object) throws IOException {
        if (object == null) return null
        return fromInputStream(object.toInputStream())
    }

    @Nullable
    static String fromInputStream(@Nullable InputStream inputStream) throws IOException {
        if (inputStream == null) return null
        return new String(inputStream.readAllBytes(), CHARSET)
    }
}
