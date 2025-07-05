plugins {
    groovy
    io.github.rassafel.build.conventions
    io.github.rassafel.developer.conventions
}

publishing {
    publications.named<MavenPublication>("mavenJava") {
        pom {
            name = "Bus"
            description = """
                Bus
            """.trimIndent()
        }
    }
}

dependencies {
    api(libs.apache.lang)

    testImplementation(libs.spock.core)
}
