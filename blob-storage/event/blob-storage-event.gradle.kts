plugins {
    groovy
    io.github.rassafel.build.conventions
    io.github.rassafel.developer.conventions
}

publishing {
    publications.named<MavenPublication>("mavenJava") {
        pom {
            name = "Blob Storage Event"
            description = """
                Blob Storage Event extension.
                This extension provide wrappers for Blob Storage, which allows to listen to events on blob storage containers and perform actions based on those events. Add support for Soft Delete with events.
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
