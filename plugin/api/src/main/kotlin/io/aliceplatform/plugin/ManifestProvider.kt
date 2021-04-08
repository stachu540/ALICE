package io.aliceplatform.plugin

import java.net.URLClassLoader
import java.util.jar.Manifest

object ManifestProvider {
  @get:JvmStatic
  val manifest: Manifest = with((javaClass.classLoader as URLClassLoader).findResource("META-INF/MANIFEST.Mf")) {
    if (this != null) {
      Manifest(this.openStream())
    } else {
      throw NullPointerException("No manifest file has been present")
    }
  }
}
