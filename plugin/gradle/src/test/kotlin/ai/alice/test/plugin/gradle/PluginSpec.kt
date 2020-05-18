package ai.alice.test.plugin.gradle

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object PluginSpec : Spek({
    describe("Engine") {
        context("provisioning detect class") {

        }

        context("throw each missing fields") {

        }

        context("generate properties") {

        }
    }
    describe("Module") {
        context("provisioning detect class") {
            val build = "build.gradle.kts" to """
                plugins {
                    id("ai.alice.module")
                }
                
                aliceModule {
                    modules {
                        create("testModule") {
                            id "ai.alice.test-discord"
                            implementationClass "alice.test.AliceTestModule"
                            requiredEngine "ai.alice.discord"
                            alias listOf("test-discord-module", "discord-module")
                        }
                    }
                }
            """.trimIndent()
            val setting = "settings.gradle.kts" to "rootProject.name = \"test\""
            val src = "src\\main\\kotlin\\alice\\test\\AliceTestModule.kt" to """package alice.test
                
                import ai.alice.api.engine.module.Module
                import ai.alice.discord.Discord
                
            """.trimIndent()

            it("should successful find class") {
//                val result = GradleRunner.create()
//                    .withPluginClasspath()
//                    .withGradleVersion("6.0")
//                    .withArguments("tasks")
//                    .buildAndFail()
            }
        }

        context("throw each missing fields") {

        }

        context("generate properties") {

        }
    }
})