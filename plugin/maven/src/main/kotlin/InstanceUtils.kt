package ai.alice.plugin.maven

import org.apache.maven.model.Dependency
import org.apache.maven.plugin.logging.Log

object InstanceUtils {

  private val group: String
  private val artifact: String
  private val version: String

  @JvmStatic
  fun apply(dependencies: List<Dependency>, log: Log) {
    if (!dependencies.any {
        it.groupId == group &&
          it.artifactId == artifact &&
          it.version == version
      })
  }
}
