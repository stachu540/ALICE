package ai.alice.internal.http

import ai.alice.api.http.IBody
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.io.input.NullInputStream
import org.apache.commons.io.input.NullReader
import java.io.InputStream
import java.io.Reader
import java.nio.ByteBuffer

internal fun byteBody(body: ByteArray, mapper: ObjectMapper) =
    object : IBody {
        override val stream: InputStream
            get() = body.inputStream()
        override val buffer: ByteBuffer
            get() = ByteBuffer.wrap(body)
        override val bytes: ByteArray
            get() = body
        override val reader: Reader
            get() = stream.reader()
        override val string: String
            get() = body.toString(Charsets.UTF_8)

        override fun <T : Any> mapTo(type: Class<T>): T = mapper.readValue(reader, type)
    }

@Suppress("UNCHECKED_CAST", "UNREACHABLE_CODE")
internal fun emptyBody() = object : IBody {
    override val stream: InputStream = NullInputStream(0)
    override val buffer: ByteBuffer = ByteBuffer.allocate(0)
    override val bytes: ByteArray = ByteArray(0)
    override val reader: Reader = NullReader(0)
    override val string: String = throw NullPointerException("Body is empty!")

    override fun <T : Any> mapTo(type: Class<T>): T = throw NullPointerException("Body is empty!")
}