package gg.norisk.heroes.common.events

import gg.norisk.heroes.common.hero.Hero
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.event.Cancellable
import net.silkmc.silk.core.event.Event
import net.silkmc.silk.core.event.EventScopeProperty

@OptIn(ExperimentalSilkApi::class)
object HeroEvents {
    open class HeroChangeEvent(val player: PlayerEntity)

    val heroChangeEvent = Event.onlySync<HeroChangeEvent>()

    open class HeroSelectEvent(val player: PlayerEntity, val hero: Hero<*>, var canSelect: Boolean = false)

    val heroSelectEvent = Event.onlySync<HeroSelectEvent>()

    open class PreKitEditorEvent(val player: ServerPlayerEntity) : Cancellable {
        override val isCancelled: EventScopeProperty<Boolean> = EventScopeProperty(false)
    }

    val preKitEditorEvent = Event.onlySync<PreKitEditorEvent>()

    open class HeroDeathEvent(val player: ServerPlayerEntity, var isValidDeath: Boolean)

    val heroDeathEvent = Event.onlySync<HeroDeathEvent>()
}
