package io.aliceplatform.plugin.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.TaskContainer

open class AliceEnginePlugin : AliceBasePlugin<AliceEngineExtension>("generateEngine") {
  override fun Project.applyExtension(extensions: ExtensionContainer) {
    extensions
  }
}

open class AliceModulePlugin : AliceBasePlugin<AliceModulesExtension>("generateModules") {
  override fun Project.applyExtension(extensions: ExtensionContainer) {
    TODO("Not yet implemented")
  }
}

abstract class AliceBasePlugin<E, T : Task>(
  private val taskName: String
) : Plugin<Project> {
  final override fun apply(target: Project) {
    with(target.pluginManager) {
      if (hasPlugin("java-library") || hasPlugin("java") || hasPlugin("kotlin")) {
        applyArtifact(target.dependencies)
        val extension = target.applyExtension(target.extensions)
        target.applyTask(target.tasks, extension)
      }
    }
  }

  private fun applyArtifact(dependencies: DependencyHandler) {
    dependencies.add("implementation", "")
  }

  protected abstract fun Project.applyExtension(extensions: ExtensionContainer): E
  protected abstract fun Project.applyTask(tasks: TaskContainer, extension: E): T
}
