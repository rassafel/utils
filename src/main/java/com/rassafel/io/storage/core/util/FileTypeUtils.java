package com.rassafel.io.storage.core.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

@UtilityClass
public class FileTypeUtils {
    private final MimeMappings INSTANCE = MimeMappings.DEFAULT;
    public final MimeType DEFAULT_MIME_TYPE = MimeTypeUtils.APPLICATION_OCTET_STREAM;

    public static MimeType getFileMimeType(String str) {
        val extension = FilenameUtils.getExtension(str);
        return getMimeType(extension);
    }

    public static MimeType getMimeType(String str) {
        val ext = INSTANCE.get(str);
        if (StringUtils.isBlank(ext)) return DEFAULT_MIME_TYPE;
        return MimeTypeUtils.parseMimeType(ext);
    }
}
