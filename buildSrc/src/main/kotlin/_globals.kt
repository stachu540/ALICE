import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import org.gradle.util.Path

val Project.ossrhUser: Property<String>
  get() = objects.property<String>().also {
    val env = System.getenv("OSSRH_USER")
    val prop = findProperty("ossrh.user")?.toString()
    if (env != null) {
      it.set(env)
    } else if (prop != null) {
      it.set(prop)
    }
  }

val Project.ossrhPassphrase: Property<String>
  get() = objects.property<String>().also {
    val env = System.getenv("OSSRH_PASSPHRASE")
    val prop = findProperty("ossrh.passphrase")?.toString()
    if (env != null) {
      it.set(env)
    } else if (prop != null) {
      it.set(prop)
    }
  }

val Project.signingPassphrase: Property<String>
  get() = objects.property<String>().also {
    val env = System.getenv("GPG_PASSPHRASE")
    val prop = findProperty("gpg.passphrase")?.toString()
    if (env != null) {
      it.set(env)
    } else if (prop != null) {
      it.set(prop)
    }
  }

val Project.githubToken: Property<String>
  get() = objects.property<String>().also {
    val env = System.getenv("GITHUB_TOKEN")
    val prop = findProperty("github.token")?.toString()
    if (env != null) {
      it.set(env)
    } else if (prop != null) {
      it.set(prop)
    }
  }

val Project.isSnapshot: Boolean
  get() = (rootProject.version as String).endsWith("-SNAPSHOT")

val Project.artifactId: String
  get() = (this as DefaultProject).identityPath.path.replace(Path.SEPARATOR, "-").let {
    rootProject.name + if (it.startsWith("-") && it.length > 1) it else ""
  }.toLowerCase()

//fun DependencyHandler.kotlinx(artifact: String, version: String) =
//  "org.jetbrains.kotlinx:kotlinx-$artifact:$version"
