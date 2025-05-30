plugins {
    groovy
    `lib-conventions`
}

dependencies {
    api(libs.apache.lang)

    testImplementation(libs.spock.core)
}
