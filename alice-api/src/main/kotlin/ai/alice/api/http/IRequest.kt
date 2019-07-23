package ai.alice.api.http

import org.apache.commons.collections4.MultiValuedMap

interface IRequest {
    val uri: String
    val method: Method
    val headers: MultiValuedMap<String, String>
    val body: IBody?

    fun setBody(body: Any?): IRequest

    fun addQueryParameters(key: String, vararg value: String) = addQueryParameters(key, value.toList())
    fun addQueryParameters(key: String, value: Collection<String>): IRequest

    fun addHeaders(key: String, vararg value: String) = addQueryParameters(key, value.toList())
    fun addHeaders(key: String, value: Collection<String>): IRequest = apply {
        headers.putAll(key, value)
    }
}
