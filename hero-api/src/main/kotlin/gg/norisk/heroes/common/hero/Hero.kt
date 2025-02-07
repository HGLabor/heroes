package gg.norisk.heroes.common.hero

import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.HeroesManager.toId
import gg.norisk.heroes.common.ability.PlayerProperty.Companion.JSON
import gg.norisk.heroes.common.db.JsonProvider
import gg.norisk.heroes.common.hero.ability.AbstractAbility
import gg.norisk.heroes.common.hero.ability.implementation.Ability
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.io.File

open class Hero(val name: String) {
    companion object {
        /**
         * Creates a new lazy hero delegate.
         *
         * @param config the config of this hero
         * @param builder the [HeroBuilder]
         */
        inline operator fun invoke(
            name: String,
            crossinline builder: HeroBuilder.() -> Unit
        ) = lazy {
            Hero(name).apply {
                HeroBuilder(this).apply(builder)
            }
        }
    }

    var internalCallbacks = InternalCallbacks()
    val internalKey = name.lowercase().replace(' ', '_')
    val icon = "textures/hero/${internalKey}/icon.png".toId()
    val description = Text.translatable("text.hero.$internalKey.description")
    var overlaySkin: Identifier? = null

    val abilities = hashMapOf<String, AbstractAbility<*>>()
    var color: Int = 0x4291AD

    fun registerAbility(ability: AbstractAbility<*>) {
        ability.hero = this
        // REMOVED AbilityKeyBindManager.initializeKeyBind(ability)
        abilities[ability.internalKey] = ability
    }

    fun getUsableAbilities(player: PlayerEntity): List<AbstractAbility<*>> {
        return abilities.values.toList()
    }

    @Serializable
    data class HeroJson(
        val internalKey: String,
        val properties: Map<String, JsonArray>
    )

    fun load(heroJson: HeroJson? = null) {
        if (baseFile.exists()) {
            val loaded = heroJson ?: JSON.decodeFromString<HeroJson>(baseFile.readText())
            for ((key, element) in loaded.properties) {
                val ability = abilities[key]
                for (jsonElement in element) {
                    val name = jsonElement.jsonObject["name"] ?: continue
                    val property = ability?.getAllProperties()?.find { it.name == name.jsonPrimitive.content }
                    property?.fromJson(jsonElement.toString())
                }
            }
        }
    }

    private val baseFolder get() = File(JsonProvider.baseFolder, "heroes/hero").apply { mkdirs() }
    private val baseFile get() = File(baseFolder, "$internalKey.json")

    fun save() {
        baseFile.writeText(JSON.encodeToString(toHeroJson()))
        logger.info("Successfully saved $internalKey")
    }

    fun toHeroJson(): HeroJson {
        val properties = buildMap {
            for ((key, ability) in abilities) {
                put(key, JsonArray(ability.getAllProperties().map { it.toJsonElement() }))
            }
        }
        return HeroJson(internalKey, properties)
    }

    inner class InternalCallbacks {
        var onTick: ((player: PlayerEntity) -> Unit)? = null
        var getSkin: ((player: PlayerEntity) -> Identifier?)? = null
        var onEnable: ((player: PlayerEntity) -> Unit)? = null
        var onDisable: ((player: PlayerEntity) -> Unit)? = null
    }
}
