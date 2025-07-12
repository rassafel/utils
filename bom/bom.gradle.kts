plugins {
    `java-platform`
    io.github.rassafel.publish.conventions
}

javaPlatform {
    allowDependencies()
}

publishing {
    publications.named<MavenPublication>("mavenJava") {
        pom {
            name = "Utils BOM"
            description = """
                Utils BOM
            """.trimIndent()
            packaging = "pom"
        }
        from(components["javaPlatform"])
    }
}

dependencies {
    val libraries: List<Project> by rootProject.extra
    constraints {
        libraries.sortedBy { it.name }
            .forEach { api(it) }
    }
}
