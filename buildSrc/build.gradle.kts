plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.diffplug.spotless") version "7.0.2"
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.25")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:7.0.2")
    implementation("io.freefair.gradle:lombok-plugin:8.13.1")
    implementation("io.freefair.git-version:io.freefair.git-version.gradle.plugin:8.13.1")
    implementation("io.freefair.aggregate-jacoco-report:io.freefair.aggregate-jacoco-report.gradle.plugin:8.13.1")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
    implementation("gradle.plugin.org.jetbrains.gradle.plugin.idea-ext:gradle-idea-ext:1.1.10")
    implementation("org.barfuin.gradle.jacocolog:org.barfuin.gradle.jacocolog.gradle.plugin:3.1.0")
}

tasks {
    compileKotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

spotless {
    kotlinGradle {
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
