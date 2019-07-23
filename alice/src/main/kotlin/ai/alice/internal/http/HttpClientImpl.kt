package ai.alice.internal.http

import ai.alice.api.Alice
import ai.alice.api.http.HttpClient
import ai.alice.api.http.IBody
import ai.alice.api.http.IRequest
import ai.alice.api.http.IResponse
import ai.alice.api.http.Method
import ai.alice.api.http.Status
import com.fasterxml.jackson.databind.ObjectMapper
import io.jooby.require
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.apache.commons.collections4.MultiMapUtils
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import java.io.IOException

class HttpClientImpl(override val root: Alice) : HttpClient {
    val client = OkHttpClient.Builder()
        .addInterceptor {
            it.proceed(it.request().newBuilder().apply {
                header(
                    "User-Agent",
                    Alice::class.java.`package`.let { "${it.implementationTitle} ${it.implementationVersion}" })
            }.build())
        }.build()
    val mapper: ObjectMapper
        get() = root.web.require(ObjectMapper::class)

    override suspend fun exchange(method: Method, url: String, request: IRequest.() -> Unit): IResponse =
        suspendCancellableCoroutine {
            client.newCall(RequestImpl(method, url.toHttpUrl().newBuilder(), mapper).apply(request).toRequest())
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        it.cancel(e)
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        it.resumeWith(Result.success(response.toResponse()))
                    }
                })
        }

    private fun RequestImpl.toRequest(): okhttp3.Request = okhttp3.Request.Builder().apply {
        url(httpUrl.build())
        method(method.name, body.toRequest())
        if (!headers.isEmpty) {
            headers.asMap().flatMap { e -> e.value.map { Pair(e.key, it) } }
                .forEach {
                    addHeaders(it.first, it.second)
                }
        }
    }.build()

    fun IBody.toRequest(): RequestBody = bytes.toRequestBody()

    fun okhttp3.Response.toResponse(): IResponse = ResponseImpl(
        ArrayListValuedHashMap<String, String>().apply {
            headers.toMultimap().entries.flatMap { e ->
                e.value.map { Pair(e.key, it) }
            }.forEach {
                put(it.first, it.second)
            }
        }.let { MultiMapUtils.unmodifiableMultiValuedMap(it) },
        Status.of(code, message),
        body.toBody() ?: emptyBody()
    )

    private fun ResponseBody?.toBody(): IBody? = this?.bytes()?.let {
        byteBody(it, mapper)
    }
}
