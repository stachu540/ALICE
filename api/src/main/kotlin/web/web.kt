package io.aliceplatform.api.web

import io.aliceplatform.api.AliceObjectOperator
import io.aliceplatform.api.Consumer
import io.aliceplatform.api.datastore.IDao
import io.aliceplatform.api.datastore.IdObject
import io.aliceplatform.api.objects.BooleanProvider
import io.aliceplatform.api.objects.Provider
import java.io.InputStream
import java.time.Instant
import java.util.Properties
import java.util.UUID

interface WebComponentFactory : AliceObjectOperator {
  val templateEngine: TemplateEngine
  fun connect(config: OpenIdConnect.Config.Factory.() -> Unit): OpenIdConnect
  fun <C : WebComponent> install(component: C)
}

interface TemplateEngine {
  fun parse(stream: InputStream, properties: Properties): String
}

interface WebComponent {
  val endpointFormat: String
  fun ServerRequest.handle(response: Consumer<ServerResponseSpec>)
}

interface ServerRequest : ServerMessage {
  val method: Method
  val endpoint: String
  val pathParameters: Map<String, String>
  val queryParameters: Map<String, List<String>>
  val fragment: String

  enum class Method {
    GET, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
  }
}

interface ServerMessage {
  val body: Any?
  val headers: Map<String, List<String>>
}

interface ServerResponseSpec : ServerMessage {
  val status: Status
  override var body: Any?

  fun addHeader(key: String, value: String)
  fun setHeader(key: String, vararg value: String)

  fun setStatus(code: Int)

  interface Status {
    val code: Int
    val message: String
  }
}

interface OpenIdConnect : WebComponent {
  val config: Config

  fun check(user: UserId): BooleanProvider
  fun authorize(csrfToken: String, code: String): Provider<in UserId>
  fun refresh(user: UserId): Provider<in UserId>
  fun revoke(user: UserId): BooleanProvider

  interface Config {
    val clientId: String
    val clientSecret: String
    val endpoint: String
    val scope: Set<String>
    val tableName: String

    interface Factory {
      var clientId: String
      var clientSecret: String
      var endpoint: String
      var tableName: String

      fun addScope(vararg scope: String)
      fun addScope(scope: Iterable<String>)
    }
  }
}

interface TokenStorage : IDao<UserId, UUID>

interface UserId : IdObject<UUID> {
  val accessToken: String
  val refreshToken: String
  val platformId: String
  val platformUid: String
  val expiresAt: Instant
}
