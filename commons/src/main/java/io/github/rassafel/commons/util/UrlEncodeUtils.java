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

package io.github.rassafel.commons.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Utility class for encoding and decoding URLs.
 */
@UtilityClass
public class UrlEncodeUtils {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Encode a string to URL format using the default UTF-8 charset.
     *
     * @param url the string to encode
     * @return the encoded string
     */
    public static String encode(String url) {
        return encode(url, DEFAULT_CHARSET);
    }

    /**
     * Encode a string to URL format using the specified charset.
     *
     * @param url     the string to encode
     * @param charset the charset to use for encoding
     * @return the encoded string
     */
    public static String encode(@NonNull String url, @NonNull Charset charset) {
        return URLEncoder.encode(url, charset);
    }

    /**
     * Decode a string from URL format using the default UTF-8 charset.
     *
     * @param url the string to decode
     * @return the decoded string
     */
    public static String decode(String url) {
        return decode(url, DEFAULT_CHARSET);
    }

    /**
     * Decode a string from URL format using the specified charset.
     *
     * @param url     the string to decode
     * @param charset the charset to use for decoding
     * @return the decoded string
     */
    public static String decode(@NonNull String url, @NonNull Charset charset) {
        return URLDecoder.decode(url, charset);
    }
}
