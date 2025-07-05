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

package io.github.rassafel.commons.web.util;

import java.math.BigInteger;
import java.util.UUID;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * Converts a UUID to a string of separated decimals.
 */
@RequiredArgsConstructor
public class UuidToSeparatedDecimalsStringConverter implements UuidToStringConverter {
    private static final BigInteger B = BigInteger.ONE.shiftLeft(64); // 2^64
    private static final BigInteger L = BigInteger.valueOf(Long.MAX_VALUE);
    private static final String DELIMITER = "-";
    @NonNull
    private final String delimiter;

    public UuidToSeparatedDecimalsStringConverter() {
        this(DELIMITER);
    }

    private static String toString(BigInteger v) {
        return StringUtils.leftPad(v.toString(), 20, "0");
    }

    private static BigInteger fromString(String x) {
        x = x.replaceFirst("^0+", "");
        return new BigInteger(x, 10);
    }

    @Override
    public String apply(UUID uuid) {
        return convertToString(uuid);
    }

    /**
     * Converts a UUID to a string of separated decimals.
     *
     * @param id the uuid to convert
     * @return a string of separated decimals
     */
    public String convertToString(@NonNull UUID id) {
        var lo = BigInteger.valueOf(id.getLeastSignificantBits());
        var hi = BigInteger.valueOf(id.getMostSignificantBits());

        if (hi.signum() < 0) hi = hi.add(B);

        if (lo.signum() < 0) lo = lo.add(B);

        return toString(hi) + delimiter + toString(lo);
    }

    /**
     * Converts a string of separated decimals to a UUID.
     *
     * @param x the string to convert
     * @return a uuid
     */
    public UUID convertFromString(@NonNull String x) {
        var split = StringUtils.split(x, delimiter);
        Assert.isTrue(split.length == 2, "parts count must be equals 2");
        var hi = fromString(split[0]);
        var lo = fromString(split[1]);

        if (L.compareTo(lo) < 0) lo = lo.subtract(B);

        if (L.compareTo(hi) < 0) hi = hi.subtract(B);

        return new UUID(hi.longValueExact(), lo.longValueExact());
    }
}
