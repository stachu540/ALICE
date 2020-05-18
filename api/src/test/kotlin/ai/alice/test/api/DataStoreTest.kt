package ai.alice.test.api

import ai.alice.api.provider.provide
import ai.alice.api.store.DaoObject
import ai.alice.api.store.DataStore
import ai.alice.test.api.synthetic.SyntheticSubject
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.assertDoesNotThrow
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.spekframework.spek2.style.specification.xdescribe
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
object DataStoreTest : Spek({
    describe("Data Storage") {
        val store = mockk<DataStore>()
        val synthetic = mockk<DaoObject<SyntheticSubject, Long>>()
        val resolve = spyk(SyntheticSubject(0L, "test"))

        every { store.create(eq(SyntheticSubject::class)) } returns synthetic
        coEvery { synthetic.get(0L) } returns provide(resolve)
        coEvery { synthetic.find(any()) } returns emptyList()

        context("DAO create") {
            it("should create Dao object") {
                assertEquals(synthetic, assertDoesNotThrow { store.create(SyntheticSubject::class) })

                verify { store.create(eq(SyntheticSubject::class)) }
            }
        }

        context("DAO context") {
            it("should get result") {
                assertEquals(assertDoesNotThrow {
                    runBlocking {
                        synthetic.get(0L)
                    }
                }, provide(resolve))
                coVerify { synthetic.get(any()) }
            }

            it("should get empty result with find") {
                runBlockingTest {
                    assertEquals(synthetic.find { }, emptyList())
                }
                coVerify { synthetic.find(any()) }
            }
        }
    }
})