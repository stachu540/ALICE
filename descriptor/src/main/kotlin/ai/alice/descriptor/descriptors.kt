package ai.alice.descriptor

import java.io.IOException
import java.io.UncheckedIOException
import java.net.URL
import java.util.*

interface Descriptor {
    val id: String
    val implementationClassName: String
    val properties: Properties
    val fileUrl: URL
}

class ModuleDescriptor(
    override val fileUrl: URL
) : Descriptor {
    override val id: String
        get() = fileUrl.file.let {
            it.substring(it.lastIndexOf('/') + 1, it.lastIndexOf('.'))
        }

    override val properties: Properties = try {
        fileUrl.openConnection().also {
            it.useCaches = false
        }.getInputStream().use {
            Properties().apply {
                load(it)
            }
        }
    } catch (e: IOException) {
        throw UncheckedIOException(e)
    }

    override val implementationClassName: String
        get() = properties.getProperty("implementation-class", "")

    val requiredModules: Array<String>
        get() = properties.getProperty("required-modules", "").let {
            if (it.isEmpty()) emptyArray() else it.split(',')
                .map { it.trim() }.toTypedArray()
        }

    val requiredEngine: String
        get() = properties.getProperty("required-engine", "")
}

class EngineDescriptor(
    override val fileUrl: URL
) : Descriptor {
    override val properties: Properties = try {
        fileUrl.openConnection().also {
            it.useCaches = false
        }.getInputStream().use {
            Properties().apply {
                load(it)
            }
        }
    } catch (e: IOException) {
        throw UncheckedIOException(e)
    }

    override val id: String
        get() = fileUrl.file.let {
            it.substring(it.lastIndexOf('/') + 1, it.lastIndexOf('.'))
        }

    val alias: String?
        get() = properties.getProperty("alias", null)

    override val implementationClassName: String
        get() = properties.getProperty("implementation-class", "")
}