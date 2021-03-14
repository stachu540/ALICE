import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.withType
import org.kohsuke.github.GitHubBuilder

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
        val license = if (repo.license != null) api.getLicense(repo.license.key)
          else null

        project.allprojects {
          pluginManager.withPlugin("maven-publish") {
            val publishing: PublishingExtension by project.extensions
            publishing.publications.withType<MavenPublication> {
              pom {
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
          }
        }
      }
    }
  }
}
