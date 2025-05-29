plugins {
    `java-library`
    `java-test-fixtures`
    idea
    jacoco
    io.freefair.lombok
    com.diffplug.spotless
}

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

val isCi = System.getenv("CI")?.isNotBlank() ?: false


idea.module {
    isDownloadSources = !isCi
    isDownloadJavadoc = !isCi
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    compileTestJava {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    test {
        useJUnitPlatform()
    }
}

spotless {
    if (plugins.hasPlugin(JavaPlugin::class)) java {
        cleanthat().version("2.20")
            .sourceCompatibility(java.sourceCompatibility.majorVersion)
            .addMutator("SafeAndConsensual")
            .addMutator("SafeButNotConsensual")
        toggleOffOn("@formatter:off", "@formatter:on")
        palantirJavaFormat("2.50.0").style("PALANTIR").formatJavadoc(true)
        formatAnnotations()
        removeUnusedImports()
        importOrder(
            "java", "javax", "", "com.rassafel",
            "\\#java", "\\#javax", "\\#", "\\#com.rassafel"
        )
        trimTrailingWhitespace()
        endWithNewline()
    }

    if (plugins.hasPlugin(GroovyPlugin::class)) groovy {
        toggleOffOn("@formatter:off", "@formatter:on")
        importOrder(
            "java", "javax", "", "com.rassafel",
            "\\#java", "\\#javax", "\\#", "\\#com.rassafel"
        )
        removeSemicolons()
        greclipse()
        excludeJava()
    }

    kotlinGradle {
        toggleOffOn("@formatter:off", "@formatter:on")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

dependencies {
    api(platform(project(":bom")))
    api("org.slf4j:slf4j-api")
}
