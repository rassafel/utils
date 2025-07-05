plugins {
    `java-platform`
    com.rassafel.publish.conventions
}

javaPlatform {
    allowDependencies()
}

dependencies {
    rootProject.subprojects
        .filter { !listOf("platform", "bom").contains(it.name) }
        .filter { it != rootProject }
        .forEach { api(it) }
}

tasks.withType<Jar> {
    manifest {

    }
    from(rootDir) {
        expand()
    }
}

publishing {
    publications {
        named<MavenPublication>("mavenJava") {
            from(components["javaPlatform"])
        }
    }
}
