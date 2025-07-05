import com.diffplug.gradle.spotless.FormatExtension

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.spotless)
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

java {
    val version = JavaVersion.toVersion(libs.versions.java.plugin.get())
    sourceCompatibility = version
    targetCompatibility = version
}

spotless {
    fun FormatExtension.defaults() {
        toggleOffOn("@formatter:off", "@formatter:on")
        leadingTabsToSpaces()
        trimTrailingWhitespace()
        endWithNewline()
    }
    java {
        defaults()
        removeUnusedImports()
    }
    kotlinGradle {
        defaults()
        ktlint()
    }
}

gradlePlugin {
    plugins {
        create("buildConventionsPlugin") {
            id = "io.github.rassafel.build.conventions"
            implementationClass = "io.github.rassafel.BuildConventionsPlugin"
        }
        create("developerConventionsPlugin") {
            id = "io.github.rassafel.developer.conventions"
            implementationClass = "io.github.rassafel.DeveloperConventionsPlugin"
        }
        create("versionPlugin") {
            id = "io.github.rassafel.version.conventions"
            implementationClass = "io.github.rassafel.version.VersionPlugin"
        }
        create("publishPlugin") {
            id = "io.github.rassafel.publish.conventions"
            implementationClass = "io.github.rassafel.publish.PublishPlugin"
        }
        create("publishModulePlugin") {
            id = "io.github.rassafel.publish.module.conventions"
            implementationClass = "io.github.rassafel.publish.ModulePublishPlugin"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(gradleApi())

    implementation(libs.gradle.kotlin)

    implementation(platform(libs.gradle.freefair.platform))
    implementation(libs.gradle.freefair.lombok)
    implementation(libs.gradle.freefair.git)
    implementation(libs.gradle.freefair.jacoco)

    implementation(libs.gradle.jacoco.log)

    implementation(libs.archunit)

    implementation(libs.gradle.spotless)

    implementation(libs.gradle.idea.ext)

    testImplementation(gradleTestKit())
    testImplementation(platform(libs.junit.platform))
    testImplementation(libs.junit.jupiter)
    testImplementation(platform(libs.assertj.platform))
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.junit.launcher)
}
