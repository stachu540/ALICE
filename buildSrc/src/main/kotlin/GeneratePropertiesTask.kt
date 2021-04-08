import java.io.File
import java.io.FileOutputStream
import java.util.Properties
import org.eclipse.jgit.api.Git
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

open class GeneratePropertiesTask : DefaultTask() {

  @OutputFile
  val targetResource: RegularFileProperty = project.objects.fileProperty()

  @TaskAction
  fun generate() {
    val config = project.projectProperties
    try {
      Git.open(project.rootDir).use { git ->
        val prefix = config.prefix.orNull.let { if (it != null) "$it." else "" }
        val properties = Properties()
        config.keys.getOrElse(emptySet()).forEach {
          properties.setProperty(prefix + it.name, it.value(project, git))
        }

        val path = config.propertiesLocation.flatMap { t ->
          targetResource.map { File(it.asFile, t).absoluteFile }
        }.get()

        if (!path.exists()) {
          with(path.parentFile) {
            if (!exists()) {
              mkdirs()
            }
          }
          path.createNewFile()
        }

        properties.store(FileOutputStream(path), null)
      }
    } finally {
      Git.shutdown()
    }
  }
}
