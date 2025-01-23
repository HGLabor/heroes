package gg.norisk.heroes.common.hero.ability


import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.HeroesManager.toId
import gg.norisk.heroes.common.ability.*
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.ability.operation.MultiplyBase
import gg.norisk.heroes.common.ability.operation.Operation
import gg.norisk.heroes.common.command.DebugCommand.sendDebugMessage
import gg.norisk.heroes.common.config.ConfigNode
import gg.norisk.heroes.common.cooldown.CooldownInfo
import gg.norisk.heroes.common.cooldown.MultipleUsesInfo
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.heroes.server.config.ConfigManagerServer.JSON
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.option.KeyBinding
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import net.silkmc.silk.core.text.literal
import java.awt.Color
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

abstract class AbstractAbility<T : Any>(name: String) : ConfigNode(name) {
    lateinit var hero: Hero<*>
    val internalKey = name.lowercase().replace(' ', '_')
    val description by lazy { Text.translatable("text.hero.${hero.internalKey}.ability.$internalKey.description") }
    val icon: Identifier by lazy { "textures/hero/${hero.internalKey}/abilities/$internalKey.png".toId() }
    var condition: ((PlayerEntity) -> Boolean)? = null

    var showInKeybindHud: Boolean = true

    @Environment(EnvType.CLIENT)
    var keyBind: KeyBinding? = null
    var properties = listOf<PlayerProperty<*>>()
    private val cooldowns = ConcurrentHashMap<UUID, CooldownInfo>()
    private val cooldownCoroutineScope by lazy { CoroutineScope(Dispatchers.IO) + SupervisorJob() }
    var cooldownProperty: CooldownProperty = buildCooldown(5.0, 5, AddValueTotal(-0.1, -0.4, -0.2, -0.8, -1.5, -1.0))
    var usageProperty: AbstractUsageProperty = SingleUseProperty(0.0, 0, "Use", MultiplyBase(listOf(0.0)))

    //atm used for holdcooldown
    open val extraProperties: List<PlayerProperty<*>> = emptyList()
    private var allProperties: List<PlayerProperty<*>>? = null

    fun getAllProperties(): List<PlayerProperty<*>> {
        //Todo das cachen?
        return buildList {
            add(cooldownProperty)
            add(usageProperty)
            addAll(extraProperties)
            addAll(properties)
        }
    }

    fun handleCooldown(player: ServerPlayerEntity): Boolean {
        if (hasCooldown(player)) {
            // Client an den cooldown erinnern!
            getCooldown(player)?.let { cooldownInfo ->
                player.sendDebugMessage("Cooldown: ${cooldownInfo.remaining}".literal)
                Networking.s2cCooldownPacket.send(cooldownInfo, player)
                return true
            }
        }
        return false
    }

    fun setCooldown(cooldownInfo: CooldownInfo, player: PlayerEntity) {
        println("DER COOLDOWN KAM REIn $cooldownInfo ${cooldownInfo.remaining} ${cooldownInfo.duration} Bekommen: ${System.currentTimeMillis()}")
        if (cooldownInfo.duration == 0L && cooldownInfo.startTime == 0L && cooldownInfo.currentTime == 0L) {
            cooldowns.remove(player.uuid)
        } else {
            cooldowns[player.uuid] = cooldownInfo
        }
    }

    fun addCooldown(player: PlayerEntity) {
        if (player !is ServerPlayerEntity) return
        //has cooldown
        if (handleCooldown(player)) return
        val uuid = player.uuid
        val currentTime = System.nanoTime()
        var startTime: Long? = null

        var multipleUsesInfo: MultipleUsesInfo? = null
        if (usageProperty is MultiUseProperty) {
            val currentUse = (usageProperty as MultiUseProperty).uses.getOrDefault(player.uuid, 0) + 1
            (usageProperty as MultiUseProperty).uses[player.uuid] = currentUse
            val maxUses = usageProperty.getValue(player.uuid).toInt()
            multipleUsesInfo = MultipleUsesInfo(currentUse, maxUses)
            if (currentUse == maxUses) {
                startTime = currentTime
                (usageProperty as MultiUseProperty).uses[player.uuid] = 0
            }
        } else if (usageProperty is SingleUseProperty) {
            startTime = currentTime
        }

        val value = cooldownProperty.getValue(player.uuid)
        logger.info("Sending Cooldown $value to ${player.gameProfile.name}")
        player.sendDebugMessage("Value: $value Level: ${cooldownProperty.getLevelInfo(player.uuid)}".literal)
        player.sendDebugMessage("Property: $cooldownProperty".literal)
        val duration = value.seconds.inWholeNanoseconds
        val cooldownInfo = CooldownInfo(
            player.id,
            duration,
            startTime,
            currentTime,
            multipleUsesInfo,
            hero.internalKey,
            internalKey,
            startTime?.let { it + duration }
        ).apply {
            this.durationString = getCooldownText(this)
        }
        logger.info("###REMAINING COOLDOWN: ${cooldownInfo.remaining} ${cooldownInfo.endTime} ${cooldownInfo.endTime?.minus(
            (cooldownInfo.startTime ?: 0L)
        )}")
        logger.info("REMAINING COOLDOWN: ${cooldownInfo.remaining} ${cooldownInfo.endTime}")
        logger.info("REMAINING COOLDOWN: ${cooldownInfo.remaining} ${cooldownInfo.endTime}")
        logger.info("REMAINING COOLDOWN: ${cooldownInfo.remaining} ${cooldownInfo.endTime}")
        cooldowns[uuid] = cooldownInfo
        Networking.s2cCooldownPacket.send(cooldownInfo, player)

        player.sendDebugMessage("Sending Cooldown: $cooldownInfo".literal)
        println(JSON.encodeToString(cooldownInfo))

        if (cooldownInfo.remaining > 0) {
            cooldownCoroutineScope.launch {
                //NO DELAY IN CREATIVE MODE FOR TESTING?
                player.sendMessage("START".literal.withColor(Color.red.rgb))
                player.sendMessage(getCooldownText(cooldownInfo)?.literal)
                if (!player.isCreative) {
                    delay(value.seconds.inWholeMilliseconds)
                }
                player.sendMessage("END".literal.withColor(Color.red.rgb))
                player.sendMessage(getCooldownText(cooldownInfo)?.literal)
                cooldowns -= uuid
                Networking.s2cCooldownPacket.send(
                    CooldownInfo(
                        player.id,
                        0,
                        0,
                        0,
                        multipleUsesInfo,
                        hero.internalKey,
                        internalKey,
                        null
                    ), player
                )
            }
        }
    }

    fun getCooldown(player: PlayerEntity): CooldownInfo? {
        return cooldowns[player.uuid]
    }

    fun hasCooldown(player: PlayerEntity): Boolean {
        val cooldownInfo = getCooldown(player) ?: return false
        player.sendDebugMessage("Cooldown: $cooldownInfo".literal)
        return cooldownInfo.remaining > 0
    }

    fun init() {
        for (property in getAllProperties()) {
            property.ability = this
            property.hero = this.hero
        }
    }

    open fun onStart(player: PlayerEntity) {
    }

    open fun onTick(player: PlayerEntity) {
    }

    protected fun buildMultipleUses(baseValue: Double, maxLevel: Int, operation: Operation): MultiUseProperty {
        return MultiUseProperty(baseValue, maxLevel, "Use", operation)
    }

    protected fun buildCooldown(baseValue: Double, maxLevel: Int, operation: Operation): CooldownProperty {
        return CooldownProperty(baseValue, maxLevel, "Cooldown", operation)
    }

    fun getCooldownText(cooldown: CooldownInfo): String? {
        val remaining = cooldown.remaining
        if (remaining > 0) {
            val builder = StringBuilder()
            remaining.nanoseconds.toComponents { days, hours, minutes, seconds, nanoseconds ->
                if (days > 0) builder.append(days).append("d ")
                if (hours > 0) builder.append(hours).append("h ")
                if (minutes > 0) builder.append(minutes).append("m ")
                builder.append(seconds).append(".")
                builder.append((nanoseconds / 1_000_000).toString().padStart(3, '0').take(1)) // Nur 2 Stellen
            }
            return builder.toString()
        }
        if (usageProperty is MultiUseProperty) {
            val multipleUseInfo = cooldown.multipleUsesInfo ?: return null
            val currentUse = multipleUseInfo.currentUse
            val maxUses = multipleUseInfo.maxUses
            val remainingUses = maxUses - currentUse
            return "$remainingUses/$maxUses"
        }

        return null
    }
}
