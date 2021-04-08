package ai.alice.plugin.maven

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

@Mojo(
  name = "engine", defaultPhase = LifecyclePhase.GENERATE_RESOURCES
)
open class AliceEngineMojo : AbstractMojo() {
  @Component
  lateinit var project: MavenProject

  @Parameter(required = true)
  lateinit var id: String

  @Parameter(name = "implementation-factory", required = true)
  lateinit var implementationFactory: String

  override fun execute() {
    log.debug("Checking dependencies")
    InstanceUtils.apply(project.dependencies, log)

    log.debug("Creating Resource for Engine")
    // TODO generate property
  }
}
