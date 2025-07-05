plugins {
    com.rassafel.version.conventions
    com.rassafel.build.conventions apply false
    com.rassafel.developer.conventions apply false
    com.rassafel.publish.module.conventions apply false
}

configure(subprojects.filter { !listOf("platform", "bom").contains(it.name) }.filter { it != project }) {
    apply(plugin = "java-library")
    apply(plugin = "com.rassafel.publish.module.conventions")
    dependencies {
        val api by configurations

        api(platform(project(":platform")))
        api(rootProject.libs.slf4j.api)
        api(rootProject.libs.jspecify)
    }
}
