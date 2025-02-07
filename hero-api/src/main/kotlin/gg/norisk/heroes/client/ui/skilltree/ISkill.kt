package gg.norisk.heroes.client.ui.skilltree

import io.wispforest.owo.ui.core.Component
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

interface ISkill {
    fun isUnlocked(player: PlayerEntity): Boolean
    fun isParentUnlocked(player: PlayerEntity): Boolean
    fun title(): Text
    fun parent(): ISkill?
    fun progress(player: PlayerEntity): Double
    fun skill()
    fun isLast(): Boolean
    fun tooltip(player: PlayerEntity): Text
    fun icon(): Component?
}