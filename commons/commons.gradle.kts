plugins {
    groovy
    `lib-conventions`
}

dependencies {
    api(libs.apache.lang)
    api(libs.spring.context)
    api(libs.aspectj)
    api(libs.spring.security.core)
    api(libs.hibernate.validation)

    compileOnly(libs.servlet)
    api(libs.spring.web)
    api(libs.spring.webmvc)

    testImplementation(libs.servlet)
    testImplementation(libs.spock.core)
    testImplementation(libs.spring.test)
}
