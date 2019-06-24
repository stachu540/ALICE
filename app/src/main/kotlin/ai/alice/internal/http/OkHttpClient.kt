package ai.alice.internal.http

import ai.alice.api.http.*
import ai.alice.api.http.ClientResponse
import ai.alice.internal.AliceEngine
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*
import java.io.IOException
import java.util.concurrent.CompletableFuture
import ai.alice.internal.http.ClientResponse as RS
import okhttp3.OkHttpClient as HTTP

class OkHttpClient(val rootEngine: AliceEngine) : HttpClient {
    private val http = HTTP()
    private val mapper = ObjectMapper().findAndRegisterModules()

    override fun exchange(
        method: Method,
        url: String,
        request: IRequest.() -> Unit
    ): CompletableFuture<ClientResponse> = CompletableFuture<ClientResponse>().apply {
        http.newCall(ClientRequest(method, url).apply(request).toRequest()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                complete(response.let {
                    RS(Status.of(it.code()),
                        if (it.body() != null) BodyBytes(mapper, it.body()!!.bytes()) else null,
                        it.headers().toMultimap().flatMap { e -> e.value.map { Pair(e.key, it) } }
                    )
                })
            }
        })
    }

    private fun ClientRequest.toRequest() = Request.Builder()
        .url(toUrl())
        .apply {
            val rbody = if (body != null) RequestBody.create(null, body!!.toBytes()) else null
            method(method.name, rbody)
            if (headers.isNotEmpty()) {
                headers.forEach { addHeader(it.first, it.second) }
            }
        }.build()
}
