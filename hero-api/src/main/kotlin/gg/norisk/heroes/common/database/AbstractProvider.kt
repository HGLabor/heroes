package gg.norisk.heroes.common.database

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.network.ServerPlayerEntity

abstract class AbstractProvider<K, V> {
    protected val cache = hashMapOf<K, V>()
    abstract suspend fun save(data: V)
    abstract suspend fun get(uuid: K): V

    abstract suspend fun onPlayerJoin(player: ServerPlayerEntity)
    abstract suspend fun onPlayerLeave(player: ServerPlayerEntity)

    abstract fun getCachedClient(uuid: K): V?

    protected suspend fun getCached(uuid: K): V? {
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT && !FabricLoader.getInstance().isDevelopmentEnvironment) {
            val cachedOnClient = getCachedClient(uuid)
            if (cachedOnClient != null) {
                return cachedOnClient
            }
        }
        return cache[uuid]
    }
}
