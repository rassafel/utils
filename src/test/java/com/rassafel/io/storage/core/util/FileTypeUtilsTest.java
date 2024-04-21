package com.rassafel.io.storage.core.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.util.MimeType;

class FileTypeUtilsTest {
    @ParameterizedTest
    @CsvSource({
        "app.js,text/javascript",
        "app.json,application/json",
        "app.xml,application/xml",
        "app.java,text/x-java-source",
        "app.png,image/png",
        "app.xxx,application/octet-stream"
    })
    public void testFileName(String fileName, String mimeType) {
        MimeType type = FileTypeUtils.getFileMimeType(fileName);
        Assertions.assertThat(type)
            .isNotNull()
            .hasToString(mimeType);
    }

    @ParameterizedTest
    @CsvSource({
        "js,text/javascript",
        "json,application/json",
        "xml,application/xml",
        "java,text/x-java-source",
        "png,image/png",
        "xxx,application/octet-stream"
    })
    public void testExt(String fileName, String mimeType) {
        MimeType type = FileTypeUtils.getMimeType(fileName);
        Assertions.assertThat(type)
            .isNotNull()
            .hasToString(mimeType);
    }
}
