plugins {
    groovy
    io.github.rassafel.build.conventions
    io.github.rassafel.developer.conventions
}

publishing {
    publications.named<MavenPublication>("mavenJava") {
        pom {
            name = "Blob Storage Security"
            description = """
                Blob Storage Security extension.
                This extension provides a way to secure access to Blob Storage.
                Provide API for implementation of security policies in your application.
            """.trimIndent()
        }
    }
}

dependencies {
    api(project(":blob-storage-core"))
    api(libs.apache.lang)

    testImplementation(testFixtures(project(":blob-storage-core")))
    testImplementation(libs.spock.core)
}
