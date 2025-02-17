package gg.norisk.ffa.server.mechanics

import com.github.juliarn.npclib.api.Npc
import com.github.juliarn.npclib.api.Position
import com.github.juliarn.npclib.api.event.AttackNpcEvent
import com.github.juliarn.npclib.api.event.ShowNpcEvent
import com.github.juliarn.npclib.api.profile.Profile
import com.github.juliarn.npclib.api.profile.ProfileProperty
import com.github.juliarn.npclib.api.protocol.meta.EntityMetadataFactory
import com.github.juliarn.npclib.common.event.DefaultInteractNpcEvent
import com.github.juliarn.npclib.fabric.FabricPlatform
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.ffa.server.FFAServer.isFFA
import gg.norisk.ffa.server.FFAServer.logger
import gg.norisk.ffa.server.selector.SelectorServerManager.setSelectorReady
import gg.norisk.ffa.server.selector.SelectorServerManager.setSoupItems
import gg.norisk.ffa.server.selector.SelectorServerManager.setUHCItems
import gg.norisk.ffa.server.world.WorldManager.isInKitEditorWorld
import gg.norisk.heroes.common.events.HeroEvents
import gg.norisk.heroes.common.ffa.KitEditorManager
import gg.norisk.heroes.common.ffa.KitEditorManager.onBack
import gg.norisk.heroes.common.ffa.KitEditorManager.onReset
import gg.norisk.heroes.common.ffa.KitEditorManager.world
import gg.norisk.heroes.common.player.InventorySorting
import gg.norisk.heroes.common.player.InventorySorting.Companion.CURRENT_VERSION
import gg.norisk.heroes.common.utils.PlayStyle
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AllowDamage
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World
import net.silkmc.silk.core.event.ServerEvents
import net.silkmc.silk.core.text.literal
import java.util.*

object KitEditor {
    val platform by lazy { FabricPlatform.minestomNpcPlatformBuilder().extension(this).actionController({}).build() }
    lateinit var backNpc: Npc<World, ServerPlayerEntity, ItemStack, Any>
    lateinit var resetNpc: Npc<World, ServerPlayerEntity, ItemStack, Any>

    fun initServer() {
        logger.info("Initializing Mode: $mode")
        HeroEvents.preKitEditorEvent.listen { event ->
            if (event.player.isFFA) {
                event.isCancelled.set(true)
            }
        }
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(AllowDamage { entity, source, amount ->
            if ((entity as? ServerPlayerEntity?)?.isInKitEditorWorld() == true) {
                return@AllowDamage false
            }
            return@AllowDamage true
        })
        KitEditorManager.onBack = {
            it.setSelectorReady()
        }
        KitEditorManager.resetInventory = {
            handleKit(it, PlayStyle.current)
        }

        ServerEvents.postStop.listen { event ->
            world = null
        }

        ServerEvents.postStart.listen { event ->
            if (world != null) {
                spawnNpcs()
                registerNpcEvents()
            }
        }
    }

    fun isUHC(): Boolean {
        return PlayStyle.current == PlayStyle.UHC
    }

    fun handleKit(player: PlayerEntity, mode: PlayStyle = PlayStyle.current) {
        when (PlayStyle.current) {
            PlayStyle.SOUP -> handleSoupKit(player)
            PlayStyle.UHC -> handleUHCKit(player)
        }
    }

    fun handleSoupKit(player: PlayerEntity) {
        player.inventory.clear()
        player.setSoupItems()
    }

    fun handleUHCKit(player: PlayerEntity) {
        player.inventory.clear()
        player.setUHCItems()
    }


    private fun registerNpcEvents() {
        val eventManager = platform.eventManager()
        eventManager.registerEventHandler(ShowNpcEvent.Post::class.java) { showEvent: ShowNpcEvent.Post ->
            val npc = showEvent.npc<World, ServerPlayerEntity, ItemStack, Any>()
            val player = showEvent.player<ServerPlayerEntity>()

            npc.changeMetadata(EntityMetadataFactory.skinLayerMetaFactory(), true).schedule(player)
        }
        eventManager.registerEventHandler(DefaultInteractNpcEvent::class.java) { showEvent: DefaultInteractNpcEvent ->
            val npc = showEvent.npc<World, ServerPlayerEntity, ItemStack, Any>()
            val player = showEvent.player<ServerPlayerEntity>()

            if (npc.entityId() == resetNpc.entityId()) {
                onReset(player)
            } else if (npc.entityId() == backNpc.entityId()) {
                onBack(player)
            }
        }
        eventManager.registerEventHandler(AttackNpcEvent::class.java) { showEvent: AttackNpcEvent ->
            val npc = showEvent.npc<World, ServerPlayerEntity, ItemStack, Any>()
            val player = showEvent.player<ServerPlayerEntity>()

            if (npc.entityId() == resetNpc.entityId()) {
                onReset(player)
            } else if (npc.entityId() == backNpc.entityId()) {
                onBack(player)
            }
        }
    }

    private fun spawnNpcs() {
        backNpc = platform
            .newNpcBuilder()
            .flag(Npc.LOOK_AT_PLAYER, true)
            .flag(Npc.HIT_WHEN_PLAYER_HITS, true)
            .flag(Npc.SNEAK_WHEN_PLAYER_SNEAKS, true)
            .position(Position.position(1.5, 90.00, 5.5, "hero-api:kit-editor"))
            .profile(
                Profile.resolved(
                    "FFA", UUID.randomUUID(), setOf(
                        ProfileProperty.property(
                            "textures",
                            "eyJ0aW1lc3RhbXAiOjE1ODQ0NjA2NDI0OTEsInByb2ZpbGVJZCI6ImIwZDRiMjhiYzFkNzQ4ODlhZjBlODY2MWNlZTk2YWFiIiwicHJvZmlsZU5hbWUiOiJNaW5lU2tpbl9vcmciLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q5MzEzZjM2OTdhMGZmZjk1MzE1NDlmMzNhNjIyZmUxOWY2MTZhN2Q1OTA3MTY2NDY1Y2EzMDYyOGMzYzFjZWEifX19",
                            "cdjJcFcAQtn6GtdJSkaLrQl2IlzUpkbDSSLl/a6/IGoJWJu7SDjZeXRKSJ55MYo5KZu38dG1dmlxiEhlF9pRfWtxW4+NXm7EI5fpKeoHBfXyxR3wJC5Yujo+9T+5TQkjAc4zGvgSQS4cRlqa231W4T77YLHCmV+E4rOVqvcXBsPomhtwckDwoD+NjfLH+PBcNkgYULgyUKSOvQVgbetgwjqrw8ZXt5LK9KWZsYKJZdUirapKwmXi/ZgD8h8z6i/K/3Qc4URjPTeqPahsr/hN/TWAGtr9TWf+iIgq91H8pau7FEMxuRgqayMlCLJD+JWjgkbK9Z6/HHJp7s7oGznn3MQy4Sj9vytRN0mLb+MsRwZ3ejOTopFfCynr7EdNSANcdJQUKk2/kjHwNSz067PSW4I+nzQA3tbFcohRkdUyDwZPs7Ajc9OadhS8W6AsQTPsNrxpNxf8yoO/vMvcIgwr/0PLI2VHUEWDVaDNUqzGDwHXn8O55ehje1ECFv5e48qFAC50xXrVJjN4Rtkq8OrjTamOSrHnm2PxlJUgthjqu6fxZZ1dBoKzMBlE56mIy9PLm0HjCS08zcQUvsK+IDW4l7ECWi1oRWrhPDt1wXD4AOlOeYln1C+KSlrBfdRNIW8bgx3pAaeI9Dm0qFpWjDZAKT/uxCs0Lwx0nySUYjM3yvo="
                        )
                    )
                )
            ).buildAndTrack()
        resetNpc = platform
            .newNpcBuilder()
            .flag(Npc.LOOK_AT_PLAYER, true)
            .flag(Npc.HIT_WHEN_PLAYER_HITS, true)
            .flag(Npc.SNEAK_WHEN_PLAYER_SNEAKS, true)
            .position(Position.position(-0.5, 90.00, 5.5, "hero-api:kit-editor"))
            .profile(
                Profile.resolved(
                    "RESET", UUID.randomUUID(), setOf(
                        ProfileProperty.property(
                            "textures",
                            "ewogICJ0aW1lc3RhbXAiIDogMTYxNjgzMjkxMTE4NSwKICAicHJvZmlsZUlkIiA6ICI1N2IzZGZiNWY4YTY0OWUyOGI1NDRlNGZmYzYzMjU2ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJYaWthcm8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGE5OWIwNWI5YTFkYjRkMjliNWU2NzNkNzdhZTU0YTc3ZWFiNjY4MTg1ODYwMzVjOGEyMDA1YWViODEwNjAyYSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                            "TUpkJfSfrwykKlZAOWnURNM17wX5P+S7OCJDQkeeQyLqETA96DzqK25JwMpTeJNFYcSrkDYGM6ba7nPAewhBkB3U8JaI2hdNvOHw0rQk2bxMAfg+B3Tp+VxqQb2YPQL0z8hqrJOKjzjINSkAhI2g2/rYXfNizXjiUn4f1WUejvKzsYrTcGV0TlqJeJeydJ0nVpo9ssLVu5ksr+6mOKElwvVEMgPV+0VdWC5XH3jOVSQUX1rjIW5aS+nf0A90GKu7ENxv0j0Cj03IsrL1ytx+ZguFk2vxywr49i2l5iXAOwo/qO7+3mHyzYkEyl/so2zbo9VTTGkVLJ/bmQPcbBEF0HLxl3v/m0QoGy2x/cMR2BlITtAKRQOO2zSzDLZmScYSFr0aOnGmO1qvQexn6/JLrZrDqqXFsjFTuATVwHnEXiHSb4DJ5kZds6X1Fy/4UdYLxry423sMfXZeg+49+qvfNJlsg4v+gbcPtQIBMoBKEq+wexa0PBnH7WxpJbKQhyyiQG1tzrcmhZmbA8d2eD8zmsGammI+DCQJmF+Cu3J2ftbkjON0hj09Ow4uy56RCbLkwJigbXf8v6vpBSG7QzxIxKvhwiQHeaku4CyQ6VjxzIMGowM5v5O4x7FZZcRQh0N70/GjMnTWDaVK6htQ+OixHNJ14ju10zbkpyrq484ZmRQ="
                        )
                    )
                )
            ).buildAndTrack()
    }
}
