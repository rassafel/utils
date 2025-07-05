plugins {
    groovy
    io.github.rassafel.build.conventions
    io.github.rassafel.developer.conventions
}

publishing {
    publications.named<MavenPublication>("mavenJava") {
        pom {
            name = "Blob Storage AWS"
            description = """
                Blob Storage implementation for AWS.
                It provides a simple and efficient way to interact with Amazon S3 storage service.
            """.trimIndent()
        }
    }
}

dependencies {
    api(project(":blob-storage-core"))
    api(libs.aws.s3)
    api(libs.apache.lang)

    testImplementation(testFixtures(project(":blob-storage-core")))
    testImplementation(libs.spock.core)
}
