plugins {
    groovy
    `java-test-fixtures`
    com.rassafel.build.conventions
    com.rassafel.developer.conventions
}

dependencies {
    api(libs.spring.boot)
    api(libs.spring.web)
    api(libs.apache.io)
    api(libs.apache.lang)

    testFixturesApi(libs.spock.core)
    testImplementation(libs.spock.core)
}
