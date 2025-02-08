package gg.norisk.heroes.common.hero

import gg.norisk.heroes.common.hero.ability.AbstractAbility
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import net.silkmc.silk.core.event.Event
import net.silkmc.silk.core.event.EventPriority
import net.silkmc.silk.core.event.MutableEventScope

class HeroBuilder(val hero: Hero) {
    var color: Int
        get() = hero.color
        set(value) {
            hero.color = value
        }
    var overlaySkin: Identifier?
        get() = hero.overlaySkin
        set(value) {
            hero.overlaySkin = value
        }

    /**
     * Executes the given [callback] if the player of the
     * [playerGetter] is the hero.
     */
    inline fun <reified T> Event<T>.heroPlayerEvent(
        crossinline playerGetter: (T) -> PlayerEntity?,
        priority: EventPriority = EventPriority.NORMAL,
        crossinline callback: context(MutableEventScope) (event: T) -> Unit
    ) {
        this.listen(priority) {
            val player = playerGetter(it) ?: return@listen
            if (player.getHero() != hero) return@listen
            callback.invoke(MutableEventScope, it)
        }
    }

    /**
     * Executes the given [callback] when the event is called
     */
    inline fun <reified T> Event<T>.heroEvent(
        priority: EventPriority = EventPriority.NORMAL,
        crossinline callback: context(MutableEventScope) (event: T) -> Unit
    ) {
        this.listen(priority) {
            callback.invoke(MutableEventScope, it)
        }
    }

    fun getSkin(callback: (player: PlayerEntity) -> Identifier) {
        hero.internalCallbacks.getSkin = callback
    }

    fun ability(ability: AbstractAbility<*>) {
        hero.registerAbility(ability)
    }
}
