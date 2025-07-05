plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform(libs.spring.platform))
    api(platform(libs.aws.platform))
    api(platform(libs.spock.platform))
}
