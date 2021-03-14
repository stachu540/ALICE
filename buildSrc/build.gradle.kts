plugins {
    `kotlin-dsl`
}

repositories {
  mavenCentral()
  mavenLocal()
  jcenter()
}

dependencies {
  implementation("org.kohsuke:github-api:1.123")
  implementation("org.eclipse.jgit:org.eclipse.jgit:5.10.0.202012080955-r")
}
