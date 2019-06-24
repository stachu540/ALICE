plugins {
    application
    shadow
}

application.mainClassName = "ai.alice.RunnerKt"

dependencies {
    core()
    `test-core`()
}