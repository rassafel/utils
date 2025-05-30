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

package com.rassafel.blobstorage.core.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

/**
 * Utility class for file type operations.
 */
@UtilityClass
@Slf4j
public class FileTypeUtils {
    public static final MimeType DEFAULT_MIME_TYPE = MimeTypeUtils.APPLICATION_OCTET_STREAM;
    private static final MimeMappings INSTANCE = MimeMappings.DEFAULT;

    /**
     * Get MIME type from file name.
     *
     * @param name file name
     * @return MIME type
     */
    public static MimeType getMimeTypeFromName(String name) {
        var extension = FilenameUtils.getExtension(name);
        return getMimeType(extension);
    }

    /**
     * Get MIME type from file extension.
     *
     * @param extension file extension
     * @return MIME type
     */
    public static MimeType getMimeType(String extension) {
        if (StringUtils.isBlank(extension)) return DEFAULT_MIME_TYPE;
        var ext = INSTANCE.get(extension);
        if (StringUtils.isBlank(ext)) return DEFAULT_MIME_TYPE;
        return MimeTypeUtils.parseMimeType(ext);
    }
}
