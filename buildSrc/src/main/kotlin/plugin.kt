import com.gradle.publish.PluginBundleExtension
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import org.kohsuke.github.GHLicense
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GHUser
import org.kohsuke.github.GitHubBuilder

fun Project.projectProperties(action: Action<PropertyExtension>) {
  extensions.configure("projectProperties", action)
}

val Project.projectProperties: PropertyExtension
  get() = extensions["projectProperties"] as PropertyExtension

open class GitHubPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    if (project == project.rootProject) {
      val github = GitHub(project)
      if (github.slug.isPresent) {
        val api = GitHubBuilder.fromEnvironment()
          .apply {
            if (github.token.isPresent) {
              withOAuthToken(github.token.get())
            }
          }.build()
        var repo = api.getRepository(github.slug.get())
        while (repo.isFork) {
          repo = repo.parent
        }
        val owner = repo.owner
        val developers = if (owner.type == "Organization") api.getOrganization(owner.login).listMembers()
          .toList() else listOf(owner)
        val contributors = repo.listContributors().filterNot {
          it.login.toLowerCase() in developers.map { it.login.toLowerCase() }
        }.filterNot {
          it.login.toLowerCase() in listOf("dependabot-preview[bot]", "github-actions[bot]", "dependabot[bot]")
        }
        val license = if (repo.license != null) api.getLicense(repo.license.key) else null

        project.allprojects {
          pluginManager.withPlugin("maven-publish") {
            val publishing: PublishingExtension by extensions
            publishing.publications.withType<MavenPublication> {
              (project to this).applyToPublication(repo, owner, developers, contributors, license)
            }
          }
          pluginManager.withPlugin("com.gradle.plugin-publish") {
            val pluginBundle: PluginBundleExtension by extensions
            pluginBundle.applyToPlugins(repo)
          }
        }
      }
    }
  }

  private fun Pair<Project, MavenPublication>.applyToPublication(
    repo: GHRepository, owner: GHUser, developers: List<GHUser>,
    contributors: List<GHRepository.Contributor>, license: GHLicense?
  ) {
    val project = first
    second.pom {
      if (owner.type == "Organization") {
        organization {
          name.set(owner.name)
          url.set("${owner.url}")
        }
      }
      url.set(repo.homepage)
      scm {
        connection.set(repo.httpTransportUrl)
        developerConnection.set(repo.sshUrl)
        url.set("${repo.htmlUrl}")
        tag.set("v${project.version}")
      }
      issueManagement {
        system.set("GitHub Issues")
        url.set("${repo.htmlUrl}/issues")
      }
      ciManagement {
        system.set("GitHub Actions")
        url.set("${repo.htmlUrl}/actions")
      }
      distributionManagement {
        downloadUrl.set("${repo.htmlUrl}/releases/tag/v${project.version}")
      }
      developers {
        developers.forEach {
          developer {
            id.set(it.login)
            name.set(it.name)
            url.set("${it.htmlUrl}")
          }
        }
      }
      contributors {
        contributors.forEach {
          contributor {
            name.set(it.login)
            url.set("${it.htmlUrl}")
          }
        }
      }
      if (license != null) {
        licenses {
          license {
            name.set(license.name)
            url.set("${license.htmlUrl}")
            distribution.set("repo")
          }
        }
      }
    }
  }

  private fun PluginBundleExtension.applyToPlugins(repo: GHRepository) {
    website = repo.homepage
    vcsUrl = "${repo.htmlUrl}"
    description = repo.description
    tags = repo.listTopics()
  }
}

open class PropertiesPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.extensions.create<PropertyExtension>("projectProperties", target)
    target.tasks.create<GeneratePropertiesTask>("generateProperties") {
      val processResources by target.tasks.getting(ProcessResources::class)
      processResources.dependsOn(this)
      outputDirectory.set(processResources.destinationDir)
    }
  }
}

val TaskContainer.generateProperties: TaskProvider<GeneratePropertiesTask>
  get() = named<GeneratePropertiesTask>("generateProperties")

fun TaskContainer.generateProperties(action: Action<GeneratePropertiesTask>): TaskProvider<GeneratePropertiesTask> =
  named("generateProperties", action::execute)
