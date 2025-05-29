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

package com.rassafel.blobstorage.core.util

import spock.lang.Specification

class FileTypeUtilsTest extends Specification {
    def "Name to MIME type"() {
        given:

        when:
        def actual = FileTypeUtils.getMimeTypeFromName(name)

        then:
        actual
        actual.toString() == expect

        where:
        name       | expect
        "app.js"   | "text/javascript"
        "app.json" | "application/json"
        "app.xml"  | "application/xml"
        "app.java" | "text/x-java-source"
        "app.png"  | "image/png"
        "app.xxx"  | "application/octet-stream"
        "app"      | "application/octet-stream"
        ""         | "application/octet-stream"
        null       | "application/octet-stream"
    }

    def "Extension to MIME type"() {
        given:

        when:
        def actual = FileTypeUtils.getMimeType(extension)

        then:
        actual
        actual.toString() == expect

        where:
        extension | expect
        "js"      | "text/javascript"
        "json"    | "application/json"
        "xml"     | "application/xml"
        "java"    | "text/x-java-source"
        "png"     | "image/png"
        "app.js"  | "application/octet-stream"
        "xxx"     | "application/octet-stream"
        ""        | "application/octet-stream"
        null      | "application/octet-stream"
    }
}
