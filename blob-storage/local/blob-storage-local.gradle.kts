plugins {
    groovy
    io.github.rassafel.build.conventions
    io.github.rassafel.developer.conventions
}

publishing {
    publications.named<MavenPublication>("mavenJava") {
        pom {
            name = "Blob Storage Local FS"
            description = """
                Blob Storage implementation for local file system.
                It provides a simple interface to interact with files on the local machine.
            """.trimIndent()
        }
    }
}

dependencies {
    api(project(":blob-storage-core"))
    api(libs.apache.io)
    api(libs.apache.lang)

    testImplementation(testFixtures(project(":blob-storage-core")))
    testImplementation(libs.spock.core)
    testImplementation(libs.groovy.nio)
}
