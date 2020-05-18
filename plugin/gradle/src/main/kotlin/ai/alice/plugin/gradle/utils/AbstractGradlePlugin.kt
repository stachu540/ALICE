package ai.alice.plugin.gradle.utils

import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class AbstractGradlePlugin<E : PluginExtension> : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.createExtension()
        project.configureTasks(extension)
        project.createGenerator(extension)
        project.validate(extension)
    }

    protected abstract fun Project.createExtension(): E
    protected abstract fun Project.configureTasks(extension: E)
    protected abstract fun Project.createGenerator(extension: E)
    protected abstract fun Project.validate(extension: E)
}