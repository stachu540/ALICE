package ai.alice.plugin.gradle.utils

import org.gradle.api.tasks.SourceSet

abstract class PluginExtension(
    mainSourceSet: SourceSet,
    testSourceSets: Array<SourceSet>
) {
    var testSourceSets: Set<SourceSet> = setOf()
        private set

    var mainSourceSet: SourceSet = mainSourceSet
        private set

    var automatedPublishing: Boolean = true

    init {
        testSourceSets(*testSourceSets)
    }

    fun mainSourceSet(mainSourceSet: SourceSet) {
        this.mainSourceSet = mainSourceSet
    }

    fun testSourceSets(vararg testSourceSets: SourceSet) {
        this.testSourceSets = setOf(*testSourceSets)
    }
}