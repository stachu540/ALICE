package ai.alice.test.api

import ai.alice.api.engine.command.Command
import ai.alice.test.api.synthetic.SyntheticCommandEvent
import ai.alice.test.api.synthetic.SyntheticMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withContext
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
object CommandTest : Spek({
    describe("Command") {
        val cmd: Command<SyntheticCommandEvent> = TestUtil.createCommand("test") {
            withContext(TestCoroutineDispatcher()) {
                assertTrue { it.command == "test" }
            }
        }
        val event = SyntheticCommandEvent(SyntheticMessage("!test"), "!")

        it("should resolve command execution") {
            runBlockingTest { cmd(event) }
        }

        it("should resolve command name") {
            assertEquals("test", cmd.name)
        }

        it("should resolve event") {
            assertEquals("!test", event.rawMessage)
        }
    }
})