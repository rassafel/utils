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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;

import com.rassafel.blobstorage.core.StoredBlobObject;

/**
 * Utility class for web operations related to blobs.
 * This class provides methods for transforming blob objects to HTTP responses.
 */
@UtilityClass
@Slf4j
public class BlobWebUtils {
    /**
     * Transform blob object to response resource
     *
     * @param blobObject blob object
     * @param status     response status
     * @param attachment if true return as attachment, else inline
     * @return response resource
     */
    public static ResponseEntity<Resource> toResponseEntity(
            StoredBlobObject blobObject, HttpStatus status, Boolean attachment) throws IOException {
        return toResponseEntity(blobObject, status, attachment, headers -> {
        });
    }

    /**
     * Capitalize attributes and put into headers
     *
     * @param blobObject blob object
     * @param prefix     header name prefix
     * @return headers customizer
     */
    public static Consumer<HttpHeaders> capitalizeCustomHeaders(StoredBlobObject blobObject, String prefix) {
        return capitalizeCustomHeaders(blobObject, prefix, s -> true);
    }

    /**
     * Capitalize attributes and put into headers
     *
     * @param blobObject      blob object
     * @param prefix          header name prefix
     * @param attributeFilter attribute filter
     * @return headers customizer
     */
    public static Consumer<HttpHeaders> capitalizeCustomHeaders(
            StoredBlobObject blobObject, String prefix, Predicate<String> attributeFilter) {
        return headers -> capitalizeKeys(blobObject.getAttributes()).forEach((k, v) -> {
            if (!attributeFilter.test(k)) return;
            headers.add(prefix + k, v);
        });
    }

    /**
     * Transform blob object to response resource
     *
     * @param blobObject     blob object
     * @param status         response status
     * @param attachment     if true return as attachment, else inline
     * @param headersMutator headers mutator
     * @return response resource
     */
    public static ResponseEntity<Resource> toResponseEntity(
            StoredBlobObject blobObject, HttpStatus status, Boolean attachment, Consumer<HttpHeaders> headersMutator)
            throws IOException {
        var inputStream = blobObject.toInputStream();
        if (inputStream == null) {
            log.debug("Input stream is null, replace with empty stream");
            inputStream = new ByteArrayInputStream(new byte[0]);
        }
        var resource = new InputStreamResource(inputStream);
        return ResponseEntity.status(status)
                .contentType(MediaType.valueOf(blobObject.getContentType()))
                .contentLength(blobObject.getSize())
                .lastModified(blobObject.getLastModifiedAt().toInstant(ZoneOffset.UTC))
                .headers(headers -> headers.setContentDisposition(contentDisposition(blobObject, attachment)))
                .headers(headersMutator)
                .body(resource);
    }

    /**
     * Evaluate content disposition header
     *
     * @param blobObject file object
     * @param attachment if true return as attachment, else inline
     * @return ContentDisposition
     */
    private static ContentDisposition contentDisposition(StoredBlobObject blobObject, Boolean attachment) {
        var builder = BooleanUtils.isTrue(attachment) ?
                ContentDisposition.attachment() : ContentDisposition.inline();
        return builder.filename(blobObject.getOriginalName(), StandardCharsets.UTF_8)
                .build();
    }

    /**
     * Capitalize keys. Use {@link #capitalize(String)}
     *
     * @param values values
     * @return map with capitalized keys
     */
    public static Map<String, String> capitalizeKeys(Map<String, String> values) {
        var result = new LinkedHashMap<String, String>(values.size());
        values.forEach((key, value) -> result.put(capitalize(key), value));
        return result;
    }

    /**
     * Capitalize string
     * <p>
     * <ul>
     *     <li>Test-Value-id -> Test-Value-Id</li>
     *     <li>test-value-id -> Test-Value-Id</li>
     *     <li>test-Value-id -> Test-Value-Id</li>
     *     <li>test--value-id -> Test--Value-Id</li>
     *     <li>test-valueId -> Test-ValueId</li>
     *     <li>test -> Test</li>
     * </ul>
     *
     * @param value string to capitalize
     * @return capitalized string
     */
    public static String capitalize(String value) {
        return Pattern.compile("(?<=-)[a-z]")
                .matcher(StringUtils.capitalize(value))
                .replaceAll(m -> m.group().toUpperCase());
    }

    /**
     * Check file is compatible with media type
     *
     * @param file          file
     * @param expectedType  check type
     * @param expectedTypes check type
     */
    public static void checkCompatible(MultipartFile file, MediaType expectedType, MediaType... expectedTypes) {
        if (file == null || file.isEmpty()) {
            log.debug("File is empty");
            throw new IllegalArgumentException("file is empty");
        }
        if (StringUtils.isBlank(file.getContentType())) {
            log.debug("Empty content type");
            throw new IllegalArgumentException("unknown content type");
        }
        var actualType = MediaType.parseMediaType(file.getContentType());
        var matchedType = Stream.concat(Stream.of(expectedType), Stream.of(expectedTypes))
                .filter(actualType::isCompatibleWith)
                .findFirst()
                .orElseThrow(() -> {
                    log.debug("Mismatch content type, expected: '{}', actual: {}", expectedType, actualType);
                    return new IllegalArgumentException("mismatch content type");
                });
        log.debug("Matched content type: {}", matchedType);
    }
}
