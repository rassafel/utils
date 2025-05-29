plugins {
    `lib-conventions`
    groovy
}

dependencies {
    api(project(":blob-storage-core"))
    api(libs.aws.s3)
    api(libs.apache.lang)

    testImplementation(testFixtures(project(":blob-storage-core")))
    testImplementation(libs.spock.core)
}
