plugins {
    id("java")
    id("groovy")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    idea
}

group = "com.rassafel"
java.sourceCompatibility = JavaVersion.VERSION_17
val isCI = System.getenv().containsKey("CI")
val amazonVersion: String by project
val spockVersion: String by project

idea.module {
    isDownloadSources = !isCI
    isDownloadJavadoc = !isCI
}

repositories { mavenCentral() }

tasks.withType<Test> {
    useJUnitPlatform()
}

//region DependencyHandler extensions
fun dependencyModuleName(module: String, version: String?): String =
    module + (version?.let { ":$it" } ?: "")

fun DependencyHandler.kotlinx(module: String, version: String? = null): String =
    dependencyModuleName("org.jetbrains.kotlinx:kotlinx-$module", version)

fun DependencyHandler.springBoot(
    module: String, version: String? = null
): String = dependencyModuleName(
    "org.springframework.boot:spring-boot-$module", version
)

fun DependencyHandler.springBootStarter(
    module: String, version: String? = null
): String = springBoot("starter-$module", version)

fun DependencyHandler.springSecurity(
    module: String, version: String? = null
): String = dependencyModuleName(
    "org.springframework.security:spring-security-$module", version
)

fun DependencyHandler.spock(
    module: String, version: String? = null
): String = dependencyModuleName(
    "org.spockframework:spock-$module", version
)
//endregion

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
    testCompileOnly {
        extendsFrom(configurations.testAnnotationProcessor.get())
    }
}

dependencyManagement {
    imports {
        mavenBom("software.amazon.awssdk:bom:$amazonVersion")
        mavenBom("org.spockframework:spock-bom:$spockVersion")
    }
}

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    implementation(springBoot("starter"))
    implementation(springBootStarter("data-jpa"))
    implementation(springBootStarter("web"))
    implementation(springBootStarter("validation"))
    implementation(springBootStarter("security"))
    implementation(springBootStarter("json"))
    implementation(springBootStarter("mail"))

    implementation("commons-io:commons-io:2.16.1")
    implementation("com.google.guava:guava:33.1.0-jre")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.apache.commons:commons-collections4:4.4")

    implementation("software.amazon.awssdk:s3")

    testImplementation(springBootStarter("test"))
    testImplementation(spock("core"))
    testImplementation(spock("spring"))
    testImplementation("net.bytebuddy:byte-buddy")
}

tasks.test {
    useJUnitPlatform()
}
