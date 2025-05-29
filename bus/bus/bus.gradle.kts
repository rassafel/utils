plugins {
    `lib-conventions`
    groovy
}

dependencies {
    api(libs.apache.lang)

    testImplementation(libs.spock.core)
}
