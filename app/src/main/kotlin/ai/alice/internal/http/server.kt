package ai.alice.internal.http

import ai.alice.api.http.*
import java.net.URI

data class HttpHandler(override val request: ServerRequest) : Handler {
    override val response: ServerResponse
        get() = HttpResponse()
}

class HttpResponse internal constructor() : ServerResponse {
    override val headers: MutableCollection<Pair<String, String>> = mutableListOf()
    override var body: IBody? = null
    override var status: Status = Status.OK

    override fun setBody(body: IBody): ServerResponse = apply {
        this.body = body
    }

    override fun setStatus(status: Status): ServerResponse = apply {
        this.status = status
    }

    override fun addHeader(key: String, value: String): ServerResponse = apply {
        this.headers.add(Pair(key, value))
    }

    override fun addHeaders(vararg header: Pair<String, String>): ServerResponse = apply {
        this.headers.addAll(headers)
    }
}

data class HttpServerRequest(
    val callback: HttpCallback,
    override val method: Method,
    override val headers: Collection<Pair<String, String>>,
    override val body: IBody?
) : ServerRequest {
    override val url: String
        get() = callback.url
    override val queryParams: Collection<Pair<String, String>>
        get() = callback.queryParameters
    override val pathVariables: Map<String, String>
        get() = callback.pathVariables

    override fun getHeader(key: String): Collection<String> =
        headers.filter { it.first.equals(key, true) }.map { it.second }

    override fun getHeader(key: String, index: Int): String = getHeader(key).toList()[index]

    override fun getQueryParam(key: String): Collection<String> =
        queryParams.filter { it.first.equals(key, true) }.map { it.second }

    override fun getQueryParam(key: String, index: Int): String = getQueryParam(key).toList()[index]
}

class HttpCallback(private val address: URI, private val variables: Map<Int, String>) {
    val url: String
        get() = address.path
    val queryParameters: Collection<Pair<String, String>>
        get() = address.query.split("&").map { it.split("=").let { Pair(it.first(), it.last()) } }
    val pathVariables: Map<String, String>
        get() = url.substring(1).split("/")
            .mapIndexed { idx, s -> Pair(if (variables.containsKey(idx)) variables[idx] else null, s) }
            .filter { it.first != null }.map { Pair(it.first!!, it.second) }.toMap()
}