package ai.alice.api.http

import java.io.IOException
import java.io.InputStream
import java.io.Reader
import java.nio.ByteBuffer
import kotlin.reflect.KClass

interface IBody {
    val stream: InputStream
    val buffer: ByteBuffer
    val bytes: ByteArray
    val reader: Reader
    val string: String

    val size: Int
        get() = bytes.size

    @Throws(IOException::class, NullPointerException::class)
    fun <T : Any> mapTo(type: Class<T>): T

    fun <T : Any> mapTo(type: KClass<T>): T = mapTo(type.java)
}
