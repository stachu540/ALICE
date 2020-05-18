package ai.alice.plugin.gradle.module

import ai.alice.descriptor.ModuleDescriptor
import ai.alice.plugin.gradle.utils.AbstractGradlePlugin
import ai.alice.plugin.gradle.utils.ModuleUtils
import ai.alice.plugin.gradle.utils.PluginExtension
import ai.alice.plugin.gradle.utils.PluginUtils
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.AbstractTask
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import org.gradle.internal.file.Deleter
import org.gradle.internal.util.PropertiesUtils
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import java.io.File
import java.io.Serializable
import java.util.*
import javax.inject.Inject
import kotlin.NoSuchElementException

class AliceModulePlugin : AbstractGradlePlugin<ModuleExtension>() {
    override fun Project.createExtension(): ModuleExtension {
        val convention = project.convention.getPlugin<JavaPluginConvention>()
        val mainSourceSet = convention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        val testSourceSets = convention.sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME)

        return extensions.create(ModuleUtils.EXTENSION_NAME, project, mainSourceSet, testSourceSets)
    }

    override fun Project.configureTasks(extension: ModuleExtension) {
        project.tasks.named<Jar>(PluginUtils.JAR_TASK_NAME) {
            val descriptors = mutableListOf<ModuleDescriptor>()
            val classes = mutableSetOf<String>()

            filesMatching(ModuleUtils.PATH_PATTERN) {
                ModuleDescriptor(file.toURI().toURL()).also {
                    if (it.implementationClassName.isNotEmpty() && it.requiredEngine.isNotEmpty()) {
                        descriptors += it
                    }
                }
            }
            filesMatching(PluginUtils.CLASSES_PATTERN) {
                classes += relativeSourcePath.toString()
            }

            appendParallelSafeAction {
                if (descriptors.isEmpty()) {
                    logger.warn(ModuleUtils.Messages.NO_DESCRIPTOR_WARNING_MESSAGE.format(path))
                } else {
                    val fileNames = mutableSetOf<String>()
                    descriptors.forEach {
                        val url = it.fileUrl.toURI()
                        val name = File(url).name
                        fileNames += name
                        val implementation = it.implementationClassName

                        if (implementation.isEmpty()) {
                            logger.warn(ModuleUtils.Messages.INVALID_DESCRIPTOR_WARNING_MESSAGE.format(path, name))
                        } else if (classes.contains(implementation.replace(Regex("\\."), "/") + ".class")) {
                            logger.warn(
                                ModuleUtils.Messages.BAD_IMPL_CLASS_WARNING_MESSAGE.format(
                                    path,
                                    name,
                                    implementation
                                )
                            )
                        }
                    }
                    extension.modules.forEach {
                        if (!name.contains("${it.id.get()}.properties")) {
                            logger.warn(
                                ModuleUtils.Messages.DECLARED_MODULE_MISSING_MESSAGE.format(
                                    path,
                                    it.name,
                                    it.id.get()
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    override fun Project.createGenerator(extension: ModuleExtension) {
        val generateDescriptor = tasks.register<GenerateModuleDescriptor>(ModuleUtils.TASK_NAME) {
            group = PluginUtils.TASK_GROUP
            description = ModuleUtils.TASK_DESCRIPTION
            declarations.set(extension.modules)
            outputDirectory.set(layout.buildDirectory.dir(name))
        }

        tasks.named<Copy>(PluginUtils.PROCESS_RESOURCES_TASK) {
            from(generateDescriptor)
            into(ModuleUtils.PATH)
        }
    }

    override fun Project.validate(extension: ModuleExtension) {
        afterEvaluate {
            extension.modules.forEach {
                if (it.id.isPresent) logger.warn(ModuleUtils.Messages.NO_ID_PRESENTS.format(name))
                if (it.implementationClass.isPresent) logger.warn(
                    ModuleUtils.Messages.NO_IMPLEMENTATION_PRESENTS.format(
                        name
                    )
                )
                if (it.requiredEngine.isPresent) logger.warn(ModuleUtils.Messages.NO_ENGINE_PRESENTS.format(name))
            }
        }
    }
}

class ModuleExtension(
    project: Project,
    mainSourceSet: SourceSet,
    testSourceSet: Array<SourceSet>
) : PluginExtension(mainSourceSet, testSourceSet) {
    constructor(
        project: Project,
        moduleSourceSet: SourceSet,
        testSourceSets: SourceSet
    ) : this(project, moduleSourceSet, arrayOf(testSourceSets))

    val modules: NamedDomainObjectContainer<AliceModule> = project.container { AliceModule(project, it) }

    fun modules(action: Action<NamedDomainObjectContainer<AliceModule>>) {
        action.execute(modules)
    }

    fun modules(action: NamedDomainObjectContainer<AliceModule>.() -> Unit) {
        action(modules)
    }
}

class AliceModule(
    project: Project,
    private val name: String
) : Named, Serializable {
    val id = project.objects.property<String>()
    val implementationClass = project.objects.property<String>()
    val requiredEngine = project.objects.property<String>()

    val displayName = project.objects.property<String>()
    val description = project.objects.property<String>()

    val alias = project.objects.setProperty<String>()
    val requiredModules = project.objects.setProperty<String>()

    override fun getName(): String = name

    infix fun id(id: String) {
        this.id.set(id)
    }

    infix fun implementationClass(implementationClass: String) {
        this.implementationClass.set(implementationClass)
    }

    infix fun requiredEngine(requiredEngine: String) {
        this.requiredEngine.set(requiredEngine)
    }

    infix fun displayName(displayName: String) {
        this.displayName.set(displayName)
    }

    infix fun description(description: String) {
        this.description.set(description)
    }

    fun alias(vararg alias: String) {
        alias(alias.toSet())
    }

    infix fun alias(alias: Iterable<String>) {
        this.alias.set(alias)
    }

    fun requiredModules(vararg requiredModules: String) {
        requiredModules(requiredModules.toSet())
    }

    infix fun requiredModules(requiredModules: Iterable<String>) {
        this.requiredModules.set(requiredModules)
    }
}

class GenerateModuleDescriptor : AbstractTask() {
    @get:Input
    val declarations: ListProperty<AliceModule> = project.objects.listProperty()

    @get:OutputDirectory
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty()

    @get:Inject
    val deleter: Deleter
        get() = throw UnsupportedOperationException("Decorator takes care of injection")

    @TaskAction
    fun generate() {
        if (declarations.isPresent) {
            val output = outputDirectory.get().asFile
            clearOutputDirectory(output)
            declarations.get().forEach {
                it.validate()
                it.generate(output)
            }
        } else {
            logger.error("No declaration presents!")
            return
        }
    }

    private fun AliceModule.validate() {
        if (!id.isPresent) {
            throw NoSuchElementException(ModuleUtils.Messages.NO_ID_PRESENTS.format(name))
        }
        if (!implementationClass.isPresent) {
            throw NoSuchElementException(ModuleUtils.Messages.NO_IMPLEMENTATION_PRESENTS.format(name))
        }
        if (!requiredModules.isPresent) {
            throw NoSuchElementException(ModuleUtils.Messages.NO_ENGINE_PRESENTS.format(name))
        }
    }

    private fun AliceModule.generate(output: File) {
        val id = id.get()
        (alias.getOrElse(emptySet()) + id).forEach {
            val file = File(output, "$it.properties")
            val properties = Properties().apply {
                if (it != id) {
                    setProperty("alias", id)
                } else {
                    setProperty("implementation-class", implementationClass.get())
                    setProperty("required-engine", requiredEngine.get())
                    if (requiredModules.isPresent) {
                        requiredModules.get().also {
                            if (it.isNotEmpty()) {
                                setProperty("required-modules", it.joinToString(","))
                            }
                        }
                    }
                }
            }
            writePropertiesTo(properties, file)
        }

    }

    private fun clearOutputDirectory(directoryToClear: File) {
        deleter.ensureEmptyDirectory(directoryToClear)
    }

    private fun writePropertiesTo(properties: Properties, descriptorFile: File) {
        PropertiesUtils.store(properties, descriptorFile)
    }
}