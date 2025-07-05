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
            id = "com.rassafel.build.conventions"
            implementationClass = "com.rassafel.BuildConventionsPlugin"
        }
        create("developerConventionsPlugin") {
            id = "com.rassafel.developer.conventions"
            implementationClass = "com.rassafel.DeveloperConventionsPlugin"
        }
        create("versionPlugin") {
            id = "com.rassafel.version.conventions"
            implementationClass = "com.rassafel.version.VersionPlugin"
        }
        create("publishPlugin") {
            id = "com.rassafel.publish.conventions"
            implementationClass = "com.rassafel.publish.PublishPlugin"
        }
        create("publishModulePlugin") {
            id = "com.rassafel.publish.module.conventions"
            implementationClass = "com.rassafel.publish.ModulePublishPlugin"
        }
    }
}
