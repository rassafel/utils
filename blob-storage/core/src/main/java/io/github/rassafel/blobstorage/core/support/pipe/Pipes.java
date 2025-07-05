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

package io.github.rassafel.blobstorage.core.support.pipe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@UtilityClass
public class Pipes {
    public static InputStreamStorePipe storeInputStream() {
        return new InputStreamStorePipe();
    }

    public static InputStreamStorePipe store(InputStream inputStream) {
        return storeInputStream().source(inputStream);
    }

    public static FileStorePipe storeFile() {
        return new FileStorePipe();
    }

    public static FileStorePipe store(File file) {
        return storeFile().source(file);
    }

    public static MultipartStorePipe storeMultipart() {
        return new MultipartStorePipe();
    }

    public static MultipartStorePipe store(MultipartFile file) {
        return storeMultipart().source(file);
    }

    public static DeletePipe delete() {
        return new DeletePipe();
    }

    public static class InputStreamStorePipe extends StorePipe<InputStream, InputStreamStorePipe> {
        protected InputStreamStorePipe() {
            closeStream(false);
        }

        @Override
        protected InputStream openSourceStream() {
            return source;
        }

        @Override
        protected InputStreamStorePipe self() {
            return this;
        }
    }

    public static class FileStorePipe extends StorePipe<File, FileStorePipe> {
        protected FileStorePipe() {
            closeStream(true);
        }

        @Override
        protected InputStream openSourceStream() throws IOException {
            return Files.newInputStream(source.toPath());
        }

        @Override
        protected FileStorePipe self() {
            return this;
        }
    }

    public static class MultipartStorePipe extends StorePipe<MultipartFile, MultipartStorePipe> {
        protected MultipartStorePipe() {
            closeStream(true);
        }

        @Override
        public MultipartStorePipe source(MultipartFile multipartFile) {
            super.source(multipartFile);
            var originalFilename = multipartFile.getOriginalFilename();
            if (StringUtils.isNotBlank(originalFilename)) {
                requestBuilder.originalName(originalFilename);
            }
            var contentType = multipartFile.getContentType();
            if (StringUtils.isNotBlank(contentType)) {
                requestBuilder.contentType(contentType);
            }
            requestBuilder.size(multipartFile.getSize());
            return this;
        }

        @Override
        protected InputStream openSourceStream() throws IOException {
            return source.getInputStream();
        }

        @Override
        protected MultipartStorePipe self() {
            return this;
        }
    }
}
