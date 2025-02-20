package gg.norisk.heroes.common.localization.minimessage

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.platform.modcommon.MinecraftClientAudiences
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.*
import net.minecraft.text.Text
import net.silkmc.silk.core.Silk

object MiniMessageUtils {
    private val audiences by lazy {
        if (FabricLoader.getInstance().environmentType == EnvType.SERVER) {
            MinecraftServerAudiences.builder(Silk.serverOrThrow).build()
        } else {
            MinecraftClientAudiences.builder().build()
        }
    }

    private val miniMessage = MiniMessage.miniMessage()

    private val tagResolver = TagResolver.builder().resolvers(
        StandardTags.defaults(),
        LaborColorTagResolver.INSTANCE
    ).build()


    fun deserialize(string: String): Text {
        val miniMessageComponent = miniMessage.deserialize(string, tagResolver)
        val text = audiences.asNative(miniMessageComponent)
        return text
    }
}

fun minimessage(string: String): Text {
    return MiniMessageUtils.deserialize(string)
}
