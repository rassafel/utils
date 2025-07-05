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

package com.rassafel.blobstorage.core;

import java.io.Serial;

import lombok.Getter;

/**
 * Not found blob exception
 */
@Getter
public class NotFoundBlobException extends StoreBlobException {
    @Serial
    private static final long serialVersionUID = -5834681445617813745L;
    private final String ref;

    public NotFoundBlobException(String ref) {
        this.ref = ref;
    }

    public NotFoundBlobException(String ref, String message) {
        super(message);
        this.ref = ref;
    }

    public NotFoundBlobException(String ref, String message, Throwable cause) {
        super(message, cause);
        this.ref = ref;
    }

    public NotFoundBlobException(String ref, Throwable cause) {
        super(cause);
        this.ref = ref;
    }
}
