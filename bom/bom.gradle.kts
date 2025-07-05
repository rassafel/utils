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
        }
        from(components["javaPlatform"])
    }
}

dependencies {
    rootProject.subprojects
        .filter { !listOf("platform", "bom").contains(it.name) }
        .filter { it != rootProject }
        .forEach { api(it) }
}
