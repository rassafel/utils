plugins {
    io.github.rassafel.version.conventions
    io.github.rassafel.build.conventions apply false
    io.github.rassafel.developer.conventions apply false
    io.github.rassafel.publish.module.conventions apply false
}

configure(subprojects.filter { !listOf("platform", "bom").contains(it.name) }.filter { it != project }) {
    apply(plugin = "java-library")
    apply(plugin = "java-test-fixtures")
    apply(plugin = "io.github.rassafel.publish.module.conventions")
    dependencies {
        val api by configurations

        api(platform(project(":platform")))
        api(rootProject.libs.slf4j.api)
        api(rootProject.libs.jspecify)
    }
}
