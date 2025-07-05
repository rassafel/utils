plugins {
    groovy
    io.github.rassafel.build.conventions
    io.github.rassafel.developer.conventions
}

dependencies {
    api(libs.apache.lang)

    testImplementation(libs.spock.core)
}
