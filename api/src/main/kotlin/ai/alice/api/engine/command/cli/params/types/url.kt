package ai.alice.api.engine.command.cli.params.types

import ai.alice.api.engine.command.cli.params.*
import java.net.URI
import java.net.URL

fun RawArgument.uri(): ArgumentProcessor<URI, URI> {
    return convert {
        URI.create(it)
    }
}

fun RawArgument.url(): ArgumentProcessor<URL, URL> {
    return convert {
        URL(it)
    }
}

fun RawOption.uri(): NullableOption<URI, URI> {
    return convert {
        URI.create(it)
    }
}

fun RawOption.url(): NullableOption<URL, URL> {
    return convert {
        URL(it)
    }
}