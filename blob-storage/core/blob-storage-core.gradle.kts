plugins {
    groovy
    `lib-conventions`
}

dependencies {
    api(libs.spring.boot)
    api(libs.spring.web)
    api(libs.apache.io)
    api(libs.apache.lang)

    testFixturesApi(libs.spock.core)
    testImplementation(libs.spock.core)
}
