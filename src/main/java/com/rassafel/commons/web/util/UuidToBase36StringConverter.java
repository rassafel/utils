package com.rassafel.commons.web.util;

import java.math.BigInteger;
import java.util.UUID;

public class UuidToBase36StringConverter {
    public String convertToString(UUID id) {
        var hex = id.toString().replace("-", "");
        return new BigInteger(hex, 16).toString(36);
    }

    public UUID convertFromString(String x) {
        var hex = new BigInteger(x, 36).toString(16);
        return UUID.fromString(hex);
    }
}
