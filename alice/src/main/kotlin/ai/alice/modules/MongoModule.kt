package ai.alice.modules

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.typesafe.config.Config
import io.jooby.*
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class MongoModule : Extension {
    override fun install(app: Jooby) {
        val config = app.environment.config.getConfig("database")
        app.services.apply {
            val mongo = KMongo.createClient(MongoClientSettings.builder().apply {
                applicationName("ALICE")
                applyConnectionString(ConnectionString("mongodb://${config.getString("host")}:${config.getString("port")}"))
            }.build()).coroutine
            app.services.putIfAbsent(CoroutineClient::class, mongo)
            app.services.put(CoroutineDatabase::class, mongo.getDatabase(config.getString("database")))
        }
    }
}