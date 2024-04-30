package com.rassafel.io.storage.core.support.pipe;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Store multipart pipe
 */
public class MultipartStorePipe<Result> extends StorePipe<Result, MultipartFile> {
    @Override
    protected InputStream openSourceStream() throws IOException {
        return source.getInputStream();
    }

    @Override
    public StorePipe<Result, MultipartFile> source(MultipartFile multipartFile) {
        val originalFilename = multipartFile.getOriginalFilename();
        if (StringUtils.isNotBlank(originalFilename)) {
            requestBuilder.originalName(originalFilename);
        }
        val contentType = multipartFile.getContentType();
        if (StringUtils.isNotBlank(contentType)) {
            requestBuilder.contentType(contentType);
        }
        requestBuilder.size(multipartFile.getSize());
        return super.source(multipartFile);
    }
}
