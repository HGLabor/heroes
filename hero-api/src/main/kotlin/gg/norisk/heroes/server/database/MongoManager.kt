package gg.norisk.heroes.server.database

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.bson.UuidRepresentation

object MongoManager {
    var isConnected: Boolean = false
    lateinit var client: MongoClient
    lateinit var database: MongoDatabase

    fun connect() {
        client = MongoClient.create(
            MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .credential(
                    MongoCredential.createCredential(
                        System.getenv("MONGODB_USER"),
                        System.getenv("MONGODB_AUTH_DATABASE"),
                        System.getenv("MONGODB_PASSWORD").toCharArray()
                    )
                ).applyToClusterSettings {
                    it.hosts(
                        listOf(
                            ServerAddress(
                                System.getenv("MONGODB_HOST"),
                                System.getenv("MONGODB_PORT").toIntOrNull() ?: 27017
                            )
                        )
                    )
                }.build()
        )
        database = client.getDatabase("ffa")
    }
}
