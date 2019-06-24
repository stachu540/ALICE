package ai.alice.api.http

import java.util.concurrent.CompletableFuture

interface HttpClient {
    fun exchange(method: Method, url: String, request: IRequest.() -> Unit = {}): CompletableFuture<ClientResponse>
    fun get(url: String, request: IRequest.() -> Unit = {}) = exchange(Method.GET, url, request)
    fun post(url: String, request: IRequest.() -> Unit = {}) = exchange(Method.POST, url, request)
    fun put(url: String, request: IRequest.() -> Unit = {}) = exchange(Method.PUT, url, request)
    fun patch(url: String, request: IRequest.() -> Unit = {}) = exchange(Method.PATCH, url, request)
    fun delete(url: String, request: IRequest.() -> Unit = {}) = exchange(Method.DELETE, url, request)
    fun options(url: String, request: IRequest.() -> Unit = {}) = exchange(Method.OPTIONS, url, request)
}

interface ClientRequest : IRequest {
    fun setBody(body: IBody): ClientRequest
    fun setHeader(key: String, vararg value: String): ClientRequest
    fun setQueryParam(key: String, vararg value: String): ClientRequest
}

interface ClientResponse : IResponse {
    val isSucessful: Boolean
        get() = status.code in 200..399
    val isError: Boolean
        get() = status.code in 400..599

    fun getHeader(key: String): Collection<String>
    fun getHeader(key: String, index: Int): String
}