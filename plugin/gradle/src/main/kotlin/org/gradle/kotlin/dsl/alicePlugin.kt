/**
 * This parts provided supports for Kotlin DSL builds for Gradle
 */
package org.gradle.kotlin.dsl

import ai.alice.plugin.gradle.engine.EngineExtension
import ai.alice.plugin.gradle.module.ModuleExtension
import ai.alice.plugin.gradle.utils.EngineUtils
import ai.alice.plugin.gradle.utils.ModuleUtils
import org.gradle.api.Action
import org.gradle.api.Project

val Project.aliceEngine: EngineExtension
    get() = extensions.getByName<EngineExtension>(EngineUtils.EXTENSION_NAME)

fun Project.aliceEngine(configure: Action<EngineExtension>) =
    extensions.configure(EngineUtils.EXTENSION_NAME, configure)

fun Project.aliceEngine(configure: EngineExtension.() -> Unit) =
    extensions.configure(EngineUtils.EXTENSION_NAME, configure)

val Project.aliceModule: ModuleExtension
    get() = extensions.getByName<ModuleExtension>(ModuleUtils.EXTENSION_NAME)

fun Project.aliceModule(configure: Action<ModuleExtension>) =
    extensions.configure(ModuleUtils.EXTENSION_NAME, configure)

fun Project.aliceModule(configure: ModuleExtension.() -> Unit) =
    extensions.configure(ModuleUtils.EXTENSION_NAME, configure)