package io.aliceplatform.server

import com.fasterxml.jackson.databind.ObjectMapper
import io.aliceplatform.api.Version
import io.aliceplatform.server.config.AliceNodesModule
import io.aliceplatform.server.objects.AliceProviderModule

internal fun Version.toJacksonVersion() =
  com.fasterxml.jackson.core.Version(
    major,
    minor,
    patch,
    if (release == Version.Release.SNAPSHOT) release.toString() else null,
    "io.aliceplatform",
    "alice-server"
  )

internal fun ObjectMapper.registerAliceModules(alice: DefaultAliceInstance) =
  registerModules(
    AliceNodesModule(alice),
    AliceProviderModule(alice)
  )
