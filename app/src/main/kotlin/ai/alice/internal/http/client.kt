package ai.alice.internal.http

import ai.alice.api.http.*
import ai.alice.api.http.ClientRequest
import ai.alice.api.http.ClientResponse
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream

class ClientResponse(
    override val status: Status,
    override val body: IBody?,
    override val headers: Collection<Pair<String, String>>
) : ClientResponse {
    override fun getHeader(key: String): Collection<String> =
        headers.filter { it.first.equals(key, true) }.map { it.second }

    override fun getHeader(key: String, index: Int): String = getHeader(key).toList()[index]
}

class BodyBytes internal constructor(private val mapper: ObjectMapper, private val bytes: ByteArray) : IBody {
    override fun toBytes(): ByteArray = bytes

    override fun toInputStream(): InputStream = bytes.inputStream()

    override fun <T : Any> to(type: Class<T>): T = mapper.readValue(bytes, type)

    override fun toString(): String = "BodyBytes[${bytes.size} bytes]"
}

class ClientRequest(
    override val method: Method,
    val url: String
) : ClientRequest {
    override val headers: MutableCollection<Pair<String, String>> = mutableListOf()
    override var body: IBody? = null
    val queryParams: MutableCollection<Pair<String, String>> = mutableListOf()

    override fun setBody(body: IBody) = apply {
        this.body = body
    }

    override fun setHeader(key: String, vararg value: String) = apply {
        headers += value.map { Pair(key, it) }
    }

    override fun setQueryParam(key: String, vararg value: String) = apply {
        queryParams += value.map { Pair(key, it) }
    }

    fun toUrl(): String =
        url + queryParams.joinToString("&", if (url.contains('?')) "&" else "?") { "${it.first}=${it.second}" }
}

