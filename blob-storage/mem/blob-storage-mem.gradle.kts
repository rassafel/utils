plugins {
    groovy
    io.github.rassafel.build.conventions
    io.github.rassafel.developer.conventions
}

publishing {
    publications.named<MavenPublication>("mavenJava") {
        pom {
            name = "Blob Storage In Memory Storage"
            description = """
                Blob Storage implementation for in memory storage.
                It provides a simple way to store file in memory.
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
