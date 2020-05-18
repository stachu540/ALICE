object RootProject {
    const val repoSlug = "stachu540/alice"
    const val githubUrl = "https://github.com/$repoSlug"
    const val ciUrl = "$githubUrl/actions"
    const val scmHttps = "scm:git:$githubUrl.git"
    const val scmSsh = "scm:git:git@github.com:$repoSlug.git"

    const val dlUrl = "$githubUrl/releases"
    const val mitLicense = "$githubUrl/blob/master/LICENCE.md"
    const val issues = "$githubUrl/issues"
    const val pullRequests = "$githubUrl/pulls"

    //    const val apiDocumentation = "$webUrl/api/latest"
    const val webUrl = githubUrl

    val tags = listOf("alice", "engine", "multipurpose", "chatbot", "bot", "chat")
}