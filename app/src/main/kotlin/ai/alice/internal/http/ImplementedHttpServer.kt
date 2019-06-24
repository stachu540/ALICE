package ai.alice.internal.http

import ai.alice.api.http.Handler
import ai.alice.api.http.HttpServer
import ai.alice.api.http.Method
import ai.alice.api.http.ServerResponse
import ai.alice.internal.AliceEngine
import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpExchange
import java.io.Closeable
import java.net.InetSocketAddress
import java.net.URI

class ImplementedHttpServer(val engine: AliceEngine) : HttpServer, Runnable, Closeable {

    override val port: Int by engine.configuration.get("server.port").asInteger.default(8080)
    override val bindAddress: String by engine.configuration.get("server.port").asString.default("0.0.0.0")
    val mapper = ObjectMapper().findAndRegisterModules()

    val handlers = mutableMapOf<String, Handler.() -> Unit>()

    private val server =
        com.sun.net.httpserver.HttpServer.create(InetSocketAddress.createUnresolved(bindAddress, port), 0).apply {
            createContext("/") { x ->
                val uri = x.requestURI
                val method = Method.valueOf(x.requestMethod)
                val body = try {
                    x.requestBody.use { it.readBytes() }.let { if (it.isNotEmpty()) BodyBytes(mapper, it) else null }
                } catch (e: Exception) {
                    null
                }
                val headers = x.requestHeaders
                handlers.filterKeys { contentMatch(uri, it) }.map { Pair(it.key, it.value) }.forEach {
                    val variables = mapVariable(it.first)
                    HttpHandler(
                        HttpServerRequest(
                            HttpCallback(uri, variables),
                            method,
                            headers.flatMap { e -> e.value.map { Pair(e.key, it) } },
                            body
                        )
                    ).apply(it.second).response.handle(x)
                }
            }
        }

    private fun ServerResponse.handle(exchange: HttpExchange) {
        headers.forEach { p -> exchange.responseHeaders.add(p.first, p.second) }
        if (body != null) {
            exchange.responseBody.write(body!!.toBytes())
        }
        exchange.sendResponseHeaders(status.code, (body?.size ?: 0).toLong())
    }

    private fun mapVariable(url: String) =
        url.substring(1).split('/')
            .mapIndexed { i, s -> Pair(i, s) }
            .filter { it.second.matches(Regex("^:(.+)$")) }.toMap()

    private fun contentMatch(uri: URI, url: String): Boolean =
        uri.path.matches(
            Regex(url.substring(1).split('/')
                .joinToString("/", "/") { if (it.startsWith(':')) "(.+)" else it })
        )

    override fun register(endpoint: String, request: Handler.() -> Unit) {
        handlers[endpoint] = request
    }

    override fun run() {
        server.start()
    }

    override fun close() {
        server.stop(0)
    }
}
