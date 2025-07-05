plugins {
    groovy
    `java-test-fixtures`
    io.github.rassafel.build.conventions
    io.github.rassafel.developer.conventions
}

dependencies {
    api(libs.spring.boot)
    api(libs.spring.web)
    api(libs.apache.io)
    api(libs.apache.lang)

    testFixturesApi(libs.spock.core)
    testImplementation(libs.spock.core)
}
