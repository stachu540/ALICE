package ai.alice.api.http

import ai.alice.api.RootComponent
import io.jooby.Kooby

interface HttpClient : RootComponent {
    suspend fun exchange(method: Method, url: String, request: IRequest.() -> Unit = {}): IResponse

    suspend fun get(url: String, request: IRequest.() -> Unit = {}) = exchange(Method.GET, url, request)
    suspend fun post(url: String, request: IRequest.() -> Unit = {}) = exchange(Method.POST, url, request)
    suspend fun put(url: String, request: IRequest.() -> Unit = {}) = exchange(Method.PUT, url, request)
    suspend fun delete(url: String, request: IRequest.() -> Unit = {}) = exchange(Method.DELETE, url, request)
    suspend fun options(url: String, request: IRequest.() -> Unit = {}) = exchange(Method.OPTIONS, url, request)
    suspend fun trace(url: String, request: IRequest.() -> Unit = {}) = exchange(Method.TRACE, url, request)
    suspend fun patch(url: String, request: IRequest.() -> Unit = {}) = exchange(Method.PATCH, url, request)
    suspend fun purge(url: String, request: IRequest.() -> Unit = {}) = exchange(Method.PURGE, url, request)
    suspend fun head(url: String, request: IRequest.() -> Unit = {}) = exchange(Method.HEAD, url, request)
}

typealias HttpServer = Kooby