package ai.alice.test

import ai.alice.di.ClassOperator
import ai.alice.di.get
import ai.alice.di.inject
import org.junit.jupiter.api.assertDoesNotThrow
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals

object DependencyInjectionTest : Spek({
    describe("Dependency Injection") {
        it("should register context") {
            assertDoesNotThrow { ClassOperator.register { SpecTest2 } }
            assertEquals(SpecTest2, assertDoesNotThrow { ClassOperator.get<SpecTest2>() })
//            assertTrue { assertDoesNotThrow { ClassOperator.get<SpecTest3>() } is SpecTest3 }
        }
        it("should get context") {
            ClassOperator.register { SpecTest }
            val lazy by assertDoesNotThrow { ClassOperator.inject<SpecTest>() }
            assertEquals(SpecTest, lazy)
        }
    }
}) {
    object SpecTest
    object SpecTest2 : SpecTest3
    interface SpecTest3
}