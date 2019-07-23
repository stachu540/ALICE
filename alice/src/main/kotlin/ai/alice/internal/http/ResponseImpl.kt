package ai.alice.internal.http

import ai.alice.api.http.IBody
import ai.alice.api.http.IResponse
import ai.alice.api.http.Status
import com.google.common.collect.Multimap
import org.apache.commons.collections4.MultiValuedMap

class ResponseImpl(
    override val headers: MultiValuedMap<String, String>,
    override val status: Status,
    override val body: IBody
) : IResponse
