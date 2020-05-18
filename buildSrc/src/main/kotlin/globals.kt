import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.getByName
import org.gradle.util.Path

val Project.globalProjects
    get() = rootProject.subprojects.filter { it.name !in arrayOf("bom", "all") }

val Project.bintrayUser: String
    get() = System.getenv("BINTRAY_USER") ?: findProperty("bintray.user").toString()

val Project.bintrayApiKey: String
    get() = System.getenv("BINTRAY_API_KEY") ?: findProperty("bintray.api_key").toString()

val Project.githubToken: String
    get() = System.getenv("GITHUB_TOKEN") ?: findProperty("github.token").toString()

val Project.isSnapshot: Boolean
    get() = (rootProject.version as String).endsWith("-SNAPSHOT")

val Project.artifactId: String
    get() = (this as DefaultProject).identityPath.path.split(Path.SEPARATOR).filter { it.isNotBlank() }
        .reversed().joinToString("-")
        .let { rootProject.name + if (it.length > 1) "-$it" else "" }
        .toLowerCase()

val Project.displayName: String
    get() = artifactId.split("-")
        .joinToString(" ")

val TaskContainer.deploy
    get() = getByName<DefaultTask>("deploy")