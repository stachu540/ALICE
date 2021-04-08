package io.aliceplatform

import ai.alice.internal.AliceImpl
import java.io.File

object Launcher {
  @JvmStatic
  fun main(args: Array<String>) {
    var file = File("alice.conf").absoluteFile
    val argv = args.iterator()
    while (argv.hasNext()) {
      val n = argv.next()
      when {
        n.equals("--config", true) -> {
          file = File(if (n.contains('=')) n.split('=')[1] else argv.next()).absoluteFile
        }
        n == "-c" -> {
          file = File(argv.next()).absoluteFile
        }
      }
    }

    injectProperties()

    val alice = AliceImpl(file)
    alice.run()
  }

  private fun injectProperties() {
    val propStream = javaClass.classLoader.getResourceAsStream("META-INF/system.properties")
    if (propStream != null) {
      System.getProperties().load(propStream)
    }
  }
}
