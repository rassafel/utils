package com.rassafel.commons.util;

import lombok.experimental.UtilityClass;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class UrlEncodeUtils {
    private final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public String encode(String url) {
        return encode(url, DEFAULT_CHARSET);
    }

    public String encode(String url, Charset charset) {
        return URLEncoder.encode(url, charset);
    }

    public String decode(String url) {
        return decode(url, DEFAULT_CHARSET);
    }

    public String decode(String url, Charset charset) {
        return URLDecoder.decode(url, charset);
    }
}
