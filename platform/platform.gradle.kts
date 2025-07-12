plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform(libs.spring.platform))
    api(platform(libs.aws.platform))
    api(platform(libs.junit.platform))
    api(platform(libs.assertj.platform))
    api(platform(libs.spock.platform))
}
