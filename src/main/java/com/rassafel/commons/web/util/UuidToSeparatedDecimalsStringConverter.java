package com.rassafel.commons.web.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.util.UUID;

public class UuidToSeparatedDecimalsStringConverter {
    private static final BigInteger B = BigInteger.ONE.shiftLeft(64); // 2^64
    private static final BigInteger L = BigInteger.valueOf(Long.MAX_VALUE);
    private static final String DELIMITER = "-";
    private final String delimiter;

    public UuidToSeparatedDecimalsStringConverter() {
        this(DELIMITER);
    }

    public UuidToSeparatedDecimalsStringConverter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String convertToString(UUID id) {
        var lo = BigInteger.valueOf(id.getLeastSignificantBits());
        var hi = BigInteger.valueOf(id.getMostSignificantBits());

        if (hi.signum() < 0)
            hi = hi.add(B);

        if (lo.signum() < 0)
            lo = lo.add(B);

        return toString(hi) + delimiter + toString(lo);
    }

    private static String toString(BigInteger v) {
        return StringUtils.leftPad(v.toString(), 20, "0");
    }

    public UUID convertFromString(String x) {
        var split = StringUtils.split(x, delimiter);
        Assert.isTrue(split.length == 2, "parts count must be equals 2");
        var hi = fromString(split[0]);
        var lo = fromString(split[1]);

        if (L.compareTo(lo) < 0)
            lo = lo.subtract(B);

        if (L.compareTo(hi) < 0)
            hi = hi.subtract(B);

        return new UUID(hi.longValueExact(), lo.longValueExact());
    }

    private static BigInteger fromString(String x) {
        x = x.replaceFirst("^0+", "");
        return new BigInteger(x, 10);
    }
}
