plugins {
    com.rassafel.build.conventions
    com.rassafel.developer.conventions
}

dependencies {
    api(project(":bus"))
    api(libs.spring.boot.autoconfigure)
}
