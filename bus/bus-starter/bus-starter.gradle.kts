plugins {
    `lib-conventions`
}

dependencies {
    api(project(":bus"))
    api(libs.spring.boot.autoconfigure)
}
