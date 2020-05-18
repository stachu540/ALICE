plugins {
    id("com.gorylenko.gradle-git-properties")
}

dependencies {
    api(config)
    `kotlin-test`
    `kotlin-test-junit5`
    api(project(":descriptor"))
    api(kotlinx("coroutines-core", Version.coroutines))
//    api("org.litote.kmongo:kmongo-coroutine:${Version.kmongo}")
    api("javax.persistence:javax.persistence-api:2.2")
}