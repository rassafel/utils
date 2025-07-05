plugins {
    io.github.rassafel.build.conventions
    io.github.rassafel.developer.conventions
}

publishing {
    publications.named<MavenPublication>("mavenJava") {
        pom {
            name = "Bus Spring Starter"
            description = """
                Spring Boot starter for the Bus.
            """.trimIndent()
        }
    }
}

dependencies {
    api(project(":bus"))
    api(libs.spring.boot.autoconfigure)
}
