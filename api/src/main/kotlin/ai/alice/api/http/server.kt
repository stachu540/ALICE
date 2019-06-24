package ai.alice.api.http

interface HttpServer {
    val port: Int
    val bindAddress: String

    fun register(endpoint: String, request: Handler.() -> Unit)
}

interface Handler {
    val request: ServerRequest
    val response: ServerResponse
}

interface ServerRequest : IRequest {
    val url: String
    val queryParams: Collection<Pair<String, String>>
    val pathVariables: Map<String, String>

    fun getHeader(key: String): Collection<String>
    fun getHeader(key: String, index: Int): String
    fun getQueryParam(key: String): Collection<String>
    fun getQueryParam(key: String, index: Int): String
    fun getPathVariable(key: String): String =
        pathVariables[key] ?: throw NullPointerException("This variable is not registered ':$key'")
}

interface ServerResponse : IResponse {
    override var status: Status
    override val headers: MutableCollection<Pair<String, String>>
    fun setBody(body: IBody): ServerResponse
    fun setStatus(status: Status): ServerResponse
    fun addHeader(key: String, value: String): ServerResponse
    fun addHeaders(vararg header: Pair<String, String>): ServerResponse
}