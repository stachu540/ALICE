package ai.alice.api.http

import org.apache.commons.collections4.MultiValuedMap

interface IResponse {
    val headers: MultiValuedMap<String, String>
    val status: Status
    val body: IBody

    fun getHeader(key: String): Collection<String> = headers.get(key)
    fun getHeader(key: String, index: Int): String? = getHeader(key).toList()[index]
}
