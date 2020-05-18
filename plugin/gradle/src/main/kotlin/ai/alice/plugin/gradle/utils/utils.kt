package ai.alice.plugin.gradle.utils

import org.gradle.internal.util.PropertiesUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

object PluginUtils {
    const val PATH = "META-INF/alice"
    const val PROCESS_RESOURCES_TASK = "processResources"
    const val TASK_GROUP = "Alice project"

    const val CLASSES_PATTERN = "**/*.class"
    const val JAR_TASK_NAME = "jar"

    val DEPENDENCY_NOTATION: String
        get() = "ai.alice:alice-api:$VERSION"

    @JvmStatic
    val VERSION: String
        get() = getProperty("project.version")

    internal val contextClassLoader: ClassLoader
        get() = Thread.currentThread().contextClassLoader

    private val properties: Properties = Properties().apply {
        load(contextClassLoader.getResourceAsStream("project.properties"))
    }

    fun getProperty(key: String): String = properties.getProperty(key, "")

    fun storeProperties(properties: Properties, file: File) =
        PropertiesUtils.store(properties, file)

    inline fun <reified T> getLogger() = LoggerFactory.getLogger(T::class.java)

}

object ModuleUtils {
    const val EXTENSION_NAME = "aliceModule"
    const val PATH = "${PluginUtils.PATH}/modules"
    const val PATH_PATTERN = "$PATH/*.properties"
    const val TASK_NAME = "moduleDescriptor"
    const val TASK_DESCRIPTION = "Generates module descriptors from plugin declarations."

    object Messages {
        const val NO_ENGINE_PRESENTS = "No required engine has been set to the module: %s"
        const val NO_IMPLEMENTATION_PRESENTS = "No implementation class has been set to the module: %s"
        const val NO_ID_PRESENTS = "No id has been set to the module: %s"
        const val DECLARED_MODULE_MISSING_MESSAGE = "%s: Could not find module descriptor at $PATH/%s.properties"
        const val BAD_IMPL_CLASS_WARNING_MESSAGE =
            "%s: A valid module descriptor was found for %s but the implementation class %s was not found in the jar."
        const val INVALID_DESCRIPTOR_WARNING_MESSAGE = "%s: A module descriptor was found for %s but it was invalid."
        const val NO_DESCRIPTOR_WARNING_MESSAGE = "%s: No valid module descriptors were found in $PATH"
    }
}

object EngineUtils {
    const val EXTENSION_NAME = "aliceEngine"
    const val TASK_NAME = "engineDescriptor"
    const val TASK_DESCRIPTION = "Generates engine descriptor from plugin declaration."
    const val PATH = "${PluginUtils.PATH}/engines"
    const val PATH_PATTERN = "$PATH/*.properties"

    object Messages {
        const val NO_IMPLEMENTATION_PRESENTS = "No implementation class has been set to the engine: %s"
        const val NO_ID_PRESENTS = "No id has been set to specific engine"
        const val BAD_IMPL_CLASS_WARNING_MESSAGE =
            "%s: A valid engine descriptor was found for %s but the implementation class %s was not found in the jar."
        const val DECLARED_ENGINE_MISSING_MESSAGE = "%s: Could not find module descriptor at ${PATH}/%s.properties"
        const val INVALID_DESCRIPTOR_WARNING_MESSAGE = "%s: A engine descriptor was found for %s but it was invalid."
        const val NO_DESCRIPTOR_WARNING_MESSAGE = "%s: No valid engine descriptor were found in $PATH"

    }
}