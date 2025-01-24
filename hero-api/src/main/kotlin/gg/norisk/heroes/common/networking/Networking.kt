package gg.norisk.heroes.common.networking

import gg.norisk.heroes.common.HeroesManager.toId
import gg.norisk.heroes.common.cooldown.CooldownInfo
import gg.norisk.heroes.common.hero.ability.AbilityPacket
import gg.norisk.heroes.common.hero.ability.AbilityPacketDescription
import gg.norisk.heroes.common.hero.ability.SkillPropertyPacket
import gg.norisk.heroes.common.networking.dto.HeroSelectorPacket
import gg.norisk.heroes.common.networking.dto.MousePacket
import net.silkmc.silk.network.packet.c2sPacket
import net.silkmc.silk.network.packet.s2cPacket

object Networking {
    val c2sAbilityPacket = c2sPacket<AbilityPacket<out AbilityPacketDescription>>("use-ability".toId())
    val s2cAbilityPacket = s2cPacket<AbilityPacket<out AbilityPacketDescription>>("use-ability".toId())

    val c2sSkillProperty = c2sPacket<SkillPropertyPacket>("skill-property".toId())
    val s2cHeroSelectorPacket = s2cPacket<HeroSelectorPacket>("hero-selector-s2c".toId())
    val c2sHeroSelectorPacket = c2sPacket<String>("hero-selector-c2s".toId())
    val c2sKitEditorRequestPacket = c2sPacket<Unit>("kit-editor-request".toId())

    val s2cCooldownPacket = s2cPacket<CooldownInfo>("cooldown".toId())

    val mousePacket = c2sPacket<MousePacket>("mouse-packet".toId())
    val mouseScrollPacket = c2sPacket<Boolean>("mouse-scroll".toId())

    //warum String?
    // java.lang.IllegalStateException: This serializer can be used only with Json format.Expected Encoder to be JsonEncoder, got class kotlinx.serialization.cbor.internal.CborMapWriter
    val s2cHeroSettingsPacket = s2cPacket<String>("hero-settings".toId())
}
