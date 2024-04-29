package com.rassafel.io.storage.core.util

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
