package gg.norisk.heroes.client.hero.ability

import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.ability.AbilityPacketDescription
import gg.norisk.heroes.common.hero.ability.AbstractAbility
import gg.norisk.heroes.common.hero.ability.implementation.HoldAbility
import gg.norisk.heroes.common.hero.ability.implementation.PressAbility
import gg.norisk.heroes.common.hero.ability.implementation.ToggleAbility
import gg.norisk.heroes.common.hero.getHero
import gg.norisk.utils.events.KeyEvents
import gg.norisk.utils.events.MouseEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.silkmc.silk.core.event.EventPriority

object AbilityKeyBindManager {
    fun initializeKeyBindListeners() {
        MouseEvents.mouseClickEvent.listen(EventPriority.FIRST) { event ->
            if (MinecraftClient.getInstance().currentScreen != null) return@listen
            val player = MinecraftClient.getInstance().player ?: return@listen
            val hero = MinecraftClient.getInstance().player?.getHero() ?: return@listen
            hero.getUsableAbilities(player).filter { it.keyBind?.matchesMouse(event.key.code) ?: false }
                .sortedByDescending { it.condition != null }.forEach { ability ->
                    val isConditionMet =
                        if (ability.condition == null) true else ability.condition?.invoke(player) == true
                    if (handleAbility(player, hero, ability, event.pressed, event.pressed) && isConditionMet) {
                        event.isCancelled.set(true)
                        return@listen
                    }
                }
        }

        KeyEvents.keyEvent.listen(EventPriority.FIRST) { event ->
            if (MinecraftClient.getInstance().currentScreen != null) return@listen
            val player = MinecraftClient.getInstance().player ?: return@listen
            val hero = MinecraftClient.getInstance().player?.getHero() ?: return@listen
            hero.getUsableAbilities(player).filter { it.keyBind?.matchesKey(event.key, event.scanCode) ?: false }
                .sortedByDescending { it.condition != null }.forEach { ability ->
                    val isConditionMet =
                        if (ability.condition == null) true else ability.condition?.invoke(player) == true
                    if (handleAbility(
                            player,
                            hero,
                            ability,
                            event.isClicked(),
                            event.isHold() && isConditionMet
                        )
                    ) {
                        event.isCancelled.set(true)
                        return@listen
                    }
                }
        }
    }

    /* TODO FUNKTIONIERT DAS GUT?
        fun initializeKeyBind(ability: AbstractAbility<*>) {
            logger.info("Initialize Keybind for Ability ${ability.internalKey}")
            val keyBind = ability.keyBind ?: return
            mouseClickEvent.listen { event ->
                if (keyBind.matchesMouse(event.key.code) && canUseAbility(ability)) {
                    handleAbility(ability, event.pressed)
                }
            }
            keyEvent.listen { event ->
                if (event.isHold()) return@listen
                if (keyBind.matchesKey(event.key, event.scanCode) && canUseAbility(ability)) {
                    handleAbility(ability, event.isClicked())
                }
            }
        }
    */

    private fun handleAbility(
        player: PlayerEntity,
        hero: Hero,
        ability: AbstractAbility<*>,
        pressed: Boolean,
        hold: Boolean,
    ): Boolean {
        when (ability) {
            is PressAbility -> {
                if (!pressed) return false
                return AbilityManagerClient.useAbility(player, hero, ability, AbilityPacketDescription.Use())
            }

            is HoldAbility -> {
                if (pressed) {
                    if (AbilityManagerClient.isUsingAbility(player, ability)) return false
                    return AbilityManagerClient.startAbility(player, hero, ability)
                } else if (hold) {
                    return false
                } else {
                    if (!AbilityManagerClient.isUsingAbility(player, ability)) return false
                    return AbilityManagerClient.endAbility(player, hero, ability)
                }
            }

            is ToggleAbility -> {
                if (pressed) return false
                return if (!AbilityManagerClient.isUsingAbility(player, ability)) {
                    AbilityManagerClient.startAbility(player, hero, ability)
                } else {
                    AbilityManagerClient.endAbility(player, hero, ability)
                }
            }

            else -> {
                return false
            }
        }
    }
}
