plugins {
    groovy
    io.github.rassafel.build.conventions
    io.github.rassafel.developer.conventions
}

publishing {
    publications.named<MavenPublication>("mavenJava") {
        pom {
            name = "Blob Storage Core"
            description = """
                Blob Storage APIs for Java.
                This library provides a set of classes and methods to interact with any type of file storage.
            """.trimIndent()
        }
    }
}

dependencies {
    api(libs.spring.boot)
    api(libs.spring.web)
    api(libs.apache.io)
    api(libs.apache.lang)

    testFixturesApi(libs.spock.core)
    testImplementation(libs.spock.core)
}
