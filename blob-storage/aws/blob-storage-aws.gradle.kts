plugins {
    groovy
    `java-test-fixtures`
    com.rassafel.build.conventions
    com.rassafel.developer.conventions
}

dependencies {
    api(project(":blob-storage-core"))
    api(libs.aws.s3)
    api(libs.apache.lang)

    testImplementation(testFixtures(project(":blob-storage-core")))
    testImplementation(libs.spock.core)
}
