package properties

import PropertyProvider
import java.time.Instant
import java.time.format.DateTimeFormatter
import org.eclipse.jgit.api.Git
import org.gradle.api.Project

class GitCommitId : PropertyProvider {
  override val name: String = "rev-id"
  override val value: Project.(Git) -> String = {
    it.log().call().first().id.abbreviate(8).name()
  }
}

class ProjectVersion : PropertyProvider {
  override val name: String = "version"
  override val value: Project.(Git) -> String = {
    "$version"
  }
}

class CreatedAt : PropertyProvider {
  override val name: String = "created-at"
  override val value: Project.(Git) -> String = {
    DateTimeFormatter.ISO_INSTANT.format(Instant.now())
  }
}
