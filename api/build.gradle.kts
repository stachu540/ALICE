plugins {
    dokka
    bintray
    artifactory
    `maven-publish`
    shadow
}

dependencies {
    base()
    `test-base`()
}