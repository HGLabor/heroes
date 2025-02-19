package gg.norisk.ffa.server.utils.luckperms

import gg.norisk.ffa.server.utils.luckperms.LuckPermsUtils.permissionData
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.user.User
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.commands.PermissionLevel

object LuckPermsUtils {
    private val LUCKPERMS by lazy { LuckPermsProvider.get() }

    val ServerPlayerEntity.lpUser: User
        get() = LUCKPERMS.getPlayerAdapter(ServerPlayerEntity::class.java).getUser(this)

    val ServerPlayerEntity.permissionData
        get() = LUCKPERMS.getPlayerAdapter(ServerPlayerEntity::class.java).getPermissionData(this)
}

fun ServerPlayerEntity.hasPermission(permission: String): Boolean {
    return runCatching { permissionData.checkPermission(permission).asBoolean() }.getOrNull()
        ?: hasPermissionLevel(PermissionLevel.COMMAND_RIGHTS.level)
}
