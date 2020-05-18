package ai.alice.test.api

import ai.alice.api.Alice
import ai.alice.api.provider.provide
import ai.alice.test.api.synthetic.SyntheticEngine
import ai.alice.test.api.synthetic.SyntheticModule
import ai.alice.test.api.synthetic.SyntheticModuleProvider
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.assertDoesNotThrow
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@ExperimentalStdlibApi
object ModuleTest : Spek({
    val alice = mockk<Alice>()
    val engine = spyk(SyntheticEngine(alice))
    val modules = spyk(SyntheticModuleProvider(engine))
    val module = spyk<SyntheticModule>()

    every { engine.modules } returns modules
    every { modules[eq(SyntheticModule::class)] } returns provide(module)
    every { modules[not(SyntheticModule::class)] } returns provide(null)
    coEvery { module.handle(any()) } returns Unit

    describe("Modules") {
        context("get module") {
            it("simple get") {
                assertTrue(modules[SyntheticModule::class].isPresent)
                verify { modules[SyntheticModule::class] }
            }

            it("direct getting without throwing") {
                assertDoesNotThrow { modules[SyntheticModule::class].get() }
                verify { modules[SyntheticModule::class] }
            }
        }
    }

    describe("Module") {
        it("apply module") {
            runBlockingTest { module.handle(engine) }
            coVerify { module.handle(any()) }
        }
    }
})