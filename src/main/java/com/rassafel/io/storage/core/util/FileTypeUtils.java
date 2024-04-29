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
    public final MimeType DEFAULT_MIME_TYPE = MimeTypeUtils.APPLICATION_OCTET_STREAM;
    private final MimeMappings INSTANCE = MimeMappings.DEFAULT;

    public static MimeType getMimeTypeFromName(String name) {
        val extension = FilenameUtils.getExtension(name);
        return getMimeType(extension);
    }

    public static MimeType getMimeType(String extension) {
        if (extension == null) return DEFAULT_MIME_TYPE;
        val ext = INSTANCE.get(extension);
        if (StringUtils.isBlank(ext)) return DEFAULT_MIME_TYPE;
        return MimeTypeUtils.parseMimeType(ext);
    }
}
