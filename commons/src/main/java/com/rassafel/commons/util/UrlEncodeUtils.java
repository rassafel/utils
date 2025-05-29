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

package com.rassafel.commons.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class UrlEncodeUtils {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private UrlEncodeUtils() {
    }

    public static String encode(String url) {
        return encode(url, DEFAULT_CHARSET);
    }

    public static String encode(String url, Charset charset) {
        return URLEncoder.encode(url, charset);
    }

    public static String decode(String url) {
        return decode(url, DEFAULT_CHARSET);
    }

    public static String decode(String url, Charset charset) {
        return URLDecoder.decode(url, charset);
    }
}
