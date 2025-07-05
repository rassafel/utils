plugins {
    groovy
    `java-test-fixtures`
    io.github.rassafel.build.conventions
    io.github.rassafel.developer.conventions
}

dependencies {
    api(project(":blob-storage-core"))
    api(libs.aws.s3)
    api(libs.apache.lang)

    testImplementation(testFixtures(project(":blob-storage-core")))
    testImplementation(libs.spock.core)
}
