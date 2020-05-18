package ai.alice.plugin.gradle.engine

import ai.alice.descriptor.ModuleDescriptor
import ai.alice.plugin.gradle.utils.*
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.AbstractTask
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.internal.file.Deleter
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import java.io.File
import java.io.Serializable
import java.util.*
import javax.inject.Inject
import kotlin.NoSuchElementException

class AliceEnginePlugin : AbstractGradlePlugin<EngineExtension>() {
    override fun Project.createExtension(): EngineExtension {
        val convention = project.convention.getPlugin<JavaPluginConvention>()
        val mainSourceSet = convention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        val testSourceSets = convention.sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME)

        return extensions.create(ModuleUtils.EXTENSION_NAME, project, mainSourceSet, testSourceSets)
    }

    override fun Project.configureTasks(extension: EngineExtension) {
        project.tasks.named<Jar>(PluginUtils.JAR_TASK_NAME) {
            val descriptors = mutableListOf<ModuleDescriptor>()
            val classes = mutableSetOf<String>()

            filesMatching(EngineUtils.PATH_PATTERN) {
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
                    logger.warn(EngineUtils.Messages.NO_DESCRIPTOR_WARNING_MESSAGE.format(path))
                } else {
                    val fileNames = mutableSetOf<String>()
                    descriptors.forEach {
                        val url = it.fileUrl.toURI()
                        val name = File(url).name
                        fileNames += name
                        val implementation = it.implementationClassName

                        if (implementation.isEmpty()) {
                            logger.warn(EngineUtils.Messages.INVALID_DESCRIPTOR_WARNING_MESSAGE.format(path, name))
                        } else if (classes.contains(implementation.replace(Regex("\\."), "/") + ".class")) {
                            logger.warn(
                                EngineUtils.Messages.BAD_IMPL_CLASS_WARNING_MESSAGE.format(
                                    path,
                                    name,
                                    implementation
                                )
                            )
                        }
                    }
                    extension.engine.also {
                        if (!name.contains("${it.id.get()}.properties")) {
                            logger.warn(EngineUtils.Messages.DECLARED_ENGINE_MISSING_MESSAGE.format(path, it.id.get()))
                        }
                    }
                }
            }
        }
    }

    override fun Project.createGenerator(extension: EngineExtension) {
        val generateDescriptor = tasks.register<GenerateEngineDescriptor>(EngineUtils.TASK_NAME) {
            group = PluginUtils.TASK_GROUP
            description = EngineUtils.TASK_DESCRIPTION
            declaration.set(extension.engine)
            outputDirectory.set(layout.buildDirectory.dir(name))
        }

        tasks.named<Copy>(PluginUtils.PROCESS_RESOURCES_TASK) {
            from(generateDescriptor)
            into(ModuleUtils.PATH)
        }
    }

    override fun Project.validate(extension: EngineExtension) {
        afterEvaluate {
            extension.engine.also {
                if (it.id.isPresent)
                    logger.warn(EngineUtils.Messages.NO_ID_PRESENTS.format(name))
                if (it.implementationClass.isPresent)
                    logger.warn(EngineUtils.Messages.NO_IMPLEMENTATION_PRESENTS.format(name))
            }
        }
    }
}

class EngineExtension(
    project: Project,
    mainSourceSet: SourceSet,
    testSourceSet: Array<SourceSet>
) : PluginExtension(mainSourceSet, testSourceSet) {
    constructor(
        project: Project,
        moduleSourceSet: SourceSet,
        testSourceSets: SourceSet
    ) : this(project, moduleSourceSet, arrayOf(testSourceSets))

    val engine: AliceEngine = AliceEngine(project)

    fun engine(action: Action<AliceEngine>) {
        action.execute(engine)
    }

    fun engine(action: AliceEngine.() -> Unit) {
        action(engine)
    }
}

class AliceEngine(
    project: Project
) : Serializable {
    val id = project.objects.property<String>()
    val implementationClass = project.objects.property<String>()
    val alias = project.objects.setProperty<String>()

    val displayName = project.objects.property<String>()
    val description = project.objects.property<String>()

    infix fun id(id: String) {
        this.id.set(id)
    }

    infix fun implementationClass(implementationClass: String) {
        this.implementationClass.set(implementationClass)
    }

    fun alias(vararg alias: String) {
        alias(alias.toSet())
    }

    infix fun alias(alias: Iterable<String>) {
        this.alias.set(alias)
    }

    infix fun displayName(displayName: String) {
        this.displayName.set(displayName)
    }

    infix fun description(description: String) {
        this.description.set(description)
    }
}

class GenerateEngineDescriptor : AbstractTask() {
    @get:Input
    val declaration: Property<AliceEngine> = project.objects.property()

    @get:OutputDirectory
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty()

    @get:Inject
    val deleter: Deleter
        get() = throw UnsupportedOperationException("Decorator takes care of injection")

    @TaskAction
    fun generate() {
        if (declaration.isPresent) {
            val output = outputDirectory.get().asFile
            clearOutputDirectory(output)
            declaration.get().also {
                it.validate()
                it.generate(output)
            }
        } else {
            logger.error("No declaration presents!")
            return
        }
    }

    private fun AliceEngine.validate() {
        if (!id.isPresent) {
            throw NoSuchElementException(EngineUtils.Messages.NO_ID_PRESENTS.format(name))
        }
        if (!implementationClass.isPresent) {
            throw NoSuchElementException(EngineUtils.Messages.NO_IMPLEMENTATION_PRESENTS.format(name))
        }
    }

    private fun AliceEngine.generate(output: File) {
        val id = id.get()
        (alias.getOrElse(emptySet()) + id).forEach {
            val file = File(output, "$it.properties")
            val properties = Properties().apply {
                if (it != id) {
                    setProperty("alias", id)
                } else {
                    setProperty("implementation-class", implementationClass.get())
                }
            }
            writePropertiesTo(properties, file)
        }
    }

    private fun clearOutputDirectory(output: File) {
        deleter.ensureEmptyDirectory(output)
    }

    private fun writePropertiesTo(properties: Properties, file: File) {
        PluginUtils.storeProperties(properties, file)
    }
}