plugins {
    io.github.rassafel.build.conventions
    io.github.rassafel.developer.conventions
}

dependencies {
    api(project(":bus"))
    api(libs.spring.boot.autoconfigure)
}
