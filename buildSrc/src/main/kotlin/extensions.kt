import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.property

open class GitHub(
  project: Project
) {
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
    } catch (_: Exception) {

    } finally {
      Git.shutdown()
    }
    this.token = token
    this.owner = owner
    this.repository = repository
  }
}
