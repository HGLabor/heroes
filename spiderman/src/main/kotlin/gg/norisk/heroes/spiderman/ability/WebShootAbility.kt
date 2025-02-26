package gg.norisk.heroes.spiderman.ability

import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.common.HeroesManager.client
import gg.norisk.heroes.common.ability.NumberProperty
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.AbilityScope
import gg.norisk.heroes.common.hero.ability.implementation.PressAbility
import gg.norisk.heroes.spiderman.entity.SwingWebEntity
import gg.norisk.heroes.spiderman.registry.EntityRegistry
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.core.Component
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.TypeFilter
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.entity.directionVector
import net.silkmc.silk.core.item.itemStack

val ropeLength = NumberProperty(50.0, 3, "Rope length", AddValueTotal(5.0, 10.0, 15.0, 20.0), 10).apply {
    icon = {
        Components.item(Items.FIREWORK_ROCKET.defaultStack)
    }
}

val webShootPower = NumberProperty(1.0, 3, "Web shoot power", AddValueTotal(1.4, 1.9, 2.5), 20).apply {
    icon = {
        Components.item(Items.FIREWORK_ROCKET.defaultStack)
    }
}

@OptIn(ExperimentalSilkApi::class)
object WebShootAbility : PressAbility("Web Shoot") {
    init {
        client {
            keyBind = HeroKeyBindings.firstKeyBind
        }
        properties = listOf(ropeLength, webShootPower)
        cooldownProperty = buildCooldown(60.0, 5, AddValueTotal(-10.0, -5.0, -5.0, -5.0, -10.0))
    }

    override fun getIconComponent(): Component {
        return Components.item(itemStack(Items.STRING) {})
    }

    override fun getBackgroundTexture(): Identifier {
        return Identifier.of("textures/block/packed_mud.png")
    }

    override fun onStart(player: PlayerEntity, abilityScope: AbilityScope) {
        if (player.world is ServerWorld) {
            val web = SwingWebEntity(EntityRegistry.SWING_WEB, player.world)
            web.setPosition(player.eyePos)
            web.owner = player
            web.velocity = player.directionVector.multiply(webShootPower.getValue(player.uuid))
            web.ropeLength = ropeLength.getValue(player.uuid)
            player.world.spawnEntity(web)
        }
    }

    override fun onDisable(player: PlayerEntity) {
        val world = player.world as ServerWorld
        for (entity in world.getEntitiesByType(TypeFilter.instanceOf(SwingWebEntity::class.java)) { true }) {
            if (entity.owner == player) {
                entity.discard()
            }
        }
    }
}
