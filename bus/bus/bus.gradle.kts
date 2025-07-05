plugins {
    groovy
    com.rassafel.build.conventions
    com.rassafel.developer.conventions
}

dependencies {
    api(libs.apache.lang)

    testImplementation(libs.spock.core)
}
