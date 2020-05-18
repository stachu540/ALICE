package ai.alice.utils

import ai.alice.AliceHikari
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarFile

@Suppress("UNCHECKED_CAST")
internal fun <T> forClassName(className: String): Class<T> = Class.forName(className) as Class<T>

fun String.format(map: Map<String, String>): String {
    var f = this

    map.forEach { (k, v) ->
        f = f.replace("{${k}}", v)
    }

    return f
}

fun AliceHikari.getProperties(path: String) =
    (this.classLoader as URLClassLoader).urLs.filter {
        it.file.endsWith(".jar")
    }.flatMap { JarFile(File(it.toURI())).entries().toList() }
        .filter {
            !it.isDirectory && it.name.endsWith(".properties") && it.name.startsWith(path)
        }
        .map {
            this.classLoader.getResource(it.name)!!
        }