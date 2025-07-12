import org.jreleaser.model.Active
import org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer

plugins {
    base
    io.github.rassafel.version.conventions
    io.github.rassafel.build.conventions apply false
    io.github.rassafel.developer.conventions apply false
    io.github.rassafel.publish.module.conventions apply false
    alias(libs.plugins.jreleaser)
}

val libraries by extra(subprojects.filter { !listOf("bom", "platform").contains(it.name) })

configure(libraries) {
    apply(plugin = "java-library")
    apply(plugin = "java-test-fixtures")
    apply(plugin = "io.github.rassafel.publish.module.conventions")

    configurations {
        val dependencyManagement = create("dependencyManagement") {
            isCanBeConsumed = false
            isCanBeResolved = false
            isVisible = false
        }
        matching { it.name.endsWith("Classpath") }.all { extendsFrom(dependencyManagement) }
    }

    dependencies {
        val api by configurations
        val dependencyManagement by configurations

        dependencyManagement(enforcedPlatform((project(":platform"))))

        api(rootProject.libs.slf4j.api)
        api(rootProject.libs.jspecify)
    }
}

jreleaser {
    gitRootSearch = true
    release {
        github {
            skipTag = true
            skipRelease = true
        }
    }
    signing {
        active = Active.ALWAYS
        armored = true
        verify = true
    }
    project {
        inceptionYear = "2024"
        author("@rassafel")
    }
    deploy {
        maven {
            mavenCentral.create("sonatype") {
                active = Active.ALWAYS
                url = "https://central.sonatype.com/api/v1/publisher"
                stagingRepository(provider { findProperty("localRepository") }
                    .orElse { layout.buildDirectory.dir("staging-deploy").get() }
                    .map { it.toString() }
                    .get())
                setAuthorization("Basic")
                applyMavenCentralRules = false
                sourceJar = false
                javadocJar = false
                sign = false
                checksums = false
                verifyPom = false
                stage = MavenCentralMavenDeployer.Stage.UPLOAD
            }
        }
    }
}
