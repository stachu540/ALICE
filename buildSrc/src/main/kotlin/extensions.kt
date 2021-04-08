import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import properties.CreatedAt
import properties.GitCommitId
import properties.ProjectVersion

open class GitHub(project: Project) {
  val token: Provider<String>
  val owner: Provider<String>
  val repository: Provider<String>

  @Suppress("UnstableApiUsage")
  val slug: Provider<String>
    get() = owner.flatMap { o -> repository.map { "$o/$it" } }

  init {
    val token = project.objects.property<String>()
    val owner = project.objects.property<String>()
    val repository = project.objects.property<String>()

    try {
      val (own, repo) = Git.open(project.rootDir).use {
        it.remoteList().call().flatMap { it.urIs }.filter {
          it.host.equals("github.com", true)
        }.map { it.path.substring(1, it.path.lastIndexOf(".git")) }
          .first().split('/', limit = 2)
      }
      owner.set(own)
      repository.set(repo)
      token.set(project.githubToken)
    } catch (_: Exception) { // ignored
    } finally {
      Git.shutdown()
    }
    this.token = token
    this.owner = owner
    this.repository = repository
  }
}

open class PropertyExtension(project: Project) {
  val prefix: Property<String> = project.objects.property()
  internal val keys: SetProperty<PropertyProvider> = project.objects.setProperty<PropertyProvider>()
    .convention(PropertyProvider.collect())
  val propertiesLocation: Property<String> = project.objects.property<String>()
    .convention("META-INF/git.properties")

  fun customProperty(name: String, value: Project.(Git) -> String) {
    keys.add(CustomPropertyProvider(name, value))
  }

  fun customProperty(name: String, value: String) {
    keys.add(CustomPropertyProvider(name) { value })
  }
}

interface PropertyProvider {
  val name: String
  val value: Project.(Git) -> String

  companion object {
    @JvmStatic
    fun collect() = setOf(GitCommitId(), ProjectVersion(), CreatedAt())
  }
}

private class CustomPropertyProvider(override val name: String, override val value: Project.(Git) -> String) :
  PropertyProvider
