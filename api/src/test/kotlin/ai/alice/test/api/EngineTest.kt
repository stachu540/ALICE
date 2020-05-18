package ai.alice.test.api

import ai.alice.api.Alice
import ai.alice.api.engine.EngineProvider
import ai.alice.api.provider.Provider
import ai.alice.api.provider.provide
import ai.alice.test.api.synthetic.SyntheticEngine
import ai.alice.test.api.synthetic.SyntheticModuleProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.assertDoesNotThrow
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object EngineTest : Spek({
    val alice = mockk<Alice>()
    val engies = mockk<EngineProvider>()
    val provider = mockk<Provider<Unit>>()
    val engine = spyk(SyntheticEngine(alice))
    val modules = SyntheticModuleProvider(engine)

    every { alice.engines } returns engies
    every { alice.engines(any()) } returns Unit

    every { engies[eq(SyntheticEngine::class)] } returns provide(engine)
    every { engies[not(SyntheticEngine::class)] } returns provide(null)
    every { engine.alice } returns alice
    every { engine.modules } returns modules

    every { provider.get() } returns Unit
    every { provider.isPresent } returns true

    describe("Alice") {
        it("get engine provider instance") {
            assertEquals(engies, alice.engines)
            alice.engines {
                assertEquals(engies, this)
            }

            verify { alice.engines(any()) }
        }
    }

    describe("Engine") {
        context("get engine") {
            it("simple get") {
                assertTrue(engies[SyntheticEngine::class].isPresent)
                verify { engies[SyntheticEngine::class] }
            }

            it("direct getting without throwing") {
                assertDoesNotThrow { engies[SyntheticEngine::class].get() }
                verify { engies[SyntheticEngine::class] }
            }
        }
    }

    describe("Provider") {
        it("should not throw if there is provided value") {
            assertDoesNotThrow { provider.get() }
            verify { provider.get() }
        }

        it("return true for presented value existence") {
            assert(provider.isPresent)
            verify { provider.isPresent }
        }
    }
})