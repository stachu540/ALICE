package ai.alice.plugin.gradle

import ai.alice.plugin.gradle.engine.AliceEnginePlugin
import ai.alice.plugin.gradle.module.AliceModulePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class AliceGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.apply<AliceEnginePlugin>()
        project.apply<AliceModulePlugin>()
    }
}