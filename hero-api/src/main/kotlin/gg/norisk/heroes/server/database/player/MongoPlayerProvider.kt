package gg.norisk.heroes.server.database.player

import com.mongodb.client.model.ReplaceOptions
import de.hglabor.utils.database.extensions.findOne
import de.hglabor.utils.database.extensions.getOrCreateCollection
import de.hglabor.utils.database.kmongo.eq
import gg.norisk.heroes.common.player.FFAPlayer
import gg.norisk.heroes.common.database.player.AbstractPlayerProvider
import gg.norisk.heroes.server.database.MongoManager
import java.util.*

class MongoPlayerProvider : AbstractPlayerProvider() {
    private val collection = MongoManager.database.getOrCreateCollection<FFAPlayer>("players")

    override suspend fun get(uuid: UUID): FFAPlayer {
        val player = getCached(uuid) ?: collection.findOne(FFAPlayer::uuid eq uuid) ?: FFAPlayer(uuid)
        cache[uuid] = player
        return player
    }

    override suspend fun save(data: FFAPlayer) {
        cache[data.uuid] = data
        collection.replaceOne(
            FFAPlayer::uuid eq data.uuid,
            data.copy(inventorySorting = null),
            ReplaceOptions().upsert(true)
        )
    }
}
