package gg.norisk.heroes.common.hero

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.heroes.common.HeroesManager.isClient
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.hero.HeroManager.HERO_KEY
import gg.norisk.heroes.common.hero.ability.AbstractAbility
import gg.norisk.heroes.common.hero.ability.task.AbilityCoroutineManager
import gg.norisk.heroes.server.config.ConfigManagerServer
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.silkmc.silk.core.Silk.server
import net.silkmc.silk.core.text.broadcastText

object HeroManager {
    val registeredHeroes: MutableMap<String, Hero<*>> = mutableMapOf()
    const val HERO_KEY = "hero"

    fun getHero(internalKey: String) = registeredHeroes[internalKey.replace(' ', '_')]

    fun registerHero(hero: Hero<*>): Boolean {
        logger.info("Register Hero ${hero.name}... on $this")
        registeredHeroes[hero.internalKey] = hero
        hero.abilities.values.forEach(AbstractAbility<*>::init)
        return true
    }

    fun reloadHeroes(vararg heroes: Hero<*>) {
        for (hero in heroes) {
            runCatching {
                hero.load()
                hero.save()
                ConfigManagerServer.sendHeroSettings(hero)
            }.onSuccess {
                server?.broadcastText("Loaded Hero ${hero.name}")
            }.onFailure {
                server?.broadcastText("Error loading Hero ${hero.name}")
            }
        }
    }
}

fun PlayerEntity.setHero(hero: Hero<*>?) {
    if (isClient && this == MinecraftClient.getInstance().player) {
        AbilityCoroutineManager.cancelClientJobs()
    } else {
        AbilityCoroutineManager.cancelServerJobs(this)
    }
    getHero()?.internalCallbacks?.onDisable?.invoke(this)
    this.setSyncedData(HERO_KEY, hero?.internalKey ?: "NONE")
    getHero()?.internalCallbacks?.onEnable?.invoke(this)
}

fun PlayerEntity.getHero(): Hero<*>? {
    return HeroManager.getHero(this.getSyncedData<String>(HERO_KEY) ?: "NONE")
}

fun PlayerEntity.isHero(hero: Hero<*>?) = this.getHero() == hero
