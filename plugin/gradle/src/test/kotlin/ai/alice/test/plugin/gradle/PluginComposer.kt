package ai.alice.test.plugin.gradle

import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

object PluginComposer {
    @TempDir
    lateinit var tempDir: Path

    fun create(root: String, files: Map<String, String>): File =
        Files.createDirectory(tempDir.resolve(root)).also { r ->
            files.forEach { (src, ctx) ->
                val s = r.resolve(src)
                if (!Files.exists(s)) {
                    Files.createFile(s).toFile().writeText(ctx)
                }
            }
        }.toFile()
}