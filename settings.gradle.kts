rootProject.name = "utils"

pluginManagement {
    val springBootVersion: String by settings

    plugins {
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version "1.1.4"
    }
}
