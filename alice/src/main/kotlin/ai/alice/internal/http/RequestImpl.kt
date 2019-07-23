package ai.alice.internal.http

import ai.alice.api.http.IBody
import ai.alice.api.http.IRequest
import ai.alice.api.http.Method
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.LinkedListMultimap
import com.google.common.collect.Multimap
import okhttp3.HttpUrl
import org.apache.commons.collections4.MultiMapUtils
import org.apache.commons.collections4.MultiValuedMap

class RequestImpl(
    override val method: Method,
    internal val httpUrl: HttpUrl.Builder,
    private val mapper: ObjectMapper
) : IRequest {
    override var body: IBody = emptyBody()

    override val uri: String
        get() = httpUrl.toString()
    override val headers: MultiValuedMap<String, String> = MultiMapUtils.newListValuedHashMap()

    override fun setBody(body: Any?): IRequest = apply {
        this.body = body?.let { byteBody(mapper.writeValueAsBytes(it), mapper) } ?: emptyBody()
    }

    override fun addQueryParameters(key: String, value: Collection<String>): IRequest = apply {
        value.forEach { httpUrl.addQueryParameter(key, it) }
    }
}