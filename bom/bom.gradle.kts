plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}"))
    api(platform("software.amazon.awssdk:bom:${libs.versions.amazon.get()}"))
    api(platform("org.spockframework:spock-bom:${libs.versions.spock.get()}"))
}
