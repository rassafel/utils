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
    compileOnly(libs.jakarta.persistence)
    api(libs.spring.web)
    api(libs.spring.webmvc)
    api(libs.spring.tx)
    api(libs.spring.data.commons)

    testImplementation(libs.servlet)
    testImplementation(libs.spock.core)
    testImplementation(libs.spring.test)
}
