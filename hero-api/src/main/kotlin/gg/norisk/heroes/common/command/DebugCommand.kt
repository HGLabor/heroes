package gg.norisk.heroes.common.command

import com.mojang.brigadier.context.CommandContext
import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.common.ability.PlayerProperty
import gg.norisk.heroes.common.command.EditPropertyCommand.editCommand
import gg.norisk.heroes.common.db.DatabaseManager
import gg.norisk.heroes.common.db.DatabaseManager.dbPlayer
import gg.norisk.heroes.common.ffa.KitEditorManager
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.HeroManager
import gg.norisk.heroes.common.hero.ability.AbstractAbility
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.heroes.common.networking.dto.HeroSelectorPacket
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.silkmc.silk.commands.PermissionLevel
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.text.literalText

object DebugCommand {
    fun initServer() {
        command("heroes") {
            requiresPermissionLevel(PermissionLevel.OWNER)
            //if (HeroesManager.isClient) {
            requires { it.server.saveProperties.dataConfiguration.enabledFeatures.contains(HeroesManager.heroesFlag) }
            // }
            runs {
                Networking.s2cHeroSelectorPacket.send(
                    HeroSelectorPacket(HeroManager.registeredHeroes.keys.toList(), true, KitEditorManager.hasKitWorld),
                    this.source.playerOrThrow
                )
            }
            literal("reload") {
                runs {
                    HeroManager.reloadHeroes(*HeroManager.registeredHeroes.values.toTypedArray())
                }
            }
            literal("xp") {
                literal("set") {
                    argument<Int>("xp") { xp ->
                        runsAsync {
                            val player = this.source.playerOrThrow
                            val cachedPlayer = DatabaseManager.provider.getCachedPlayer(player.uuid)
                            cachedPlayer.xp = xp()
                            player.dbPlayer = cachedPlayer
                            DatabaseManager.provider.save(player.uuid)
                        }
                    }
                }
            }
            argument<String>("hero") { heroKey ->
                suggestList { HeroManager.registeredHeroes.keys }
                literal("ability") {
                    argument<String>("ability") { abilityKey ->
                        suggestList {
                            HeroManager.getHero(heroKey(it))?.abilities?.keys
                        }
                        literal("property") {
                            argument<String>("property") { propertyKey ->
                                suggestList {
                                    HeroManager.getHero(heroKey(it))?.abilities
                                        ?.values
                                        ?.map { ability -> ability.getAllProperties() }
                                        ?.flatten()?.map { property -> property.internalKey }
                                }
                                editCommand()
                                literal("add") {
                                    argument<Int>("expierencepoints") { xpPoints ->
                                        runs {
                                            val hero = HeroManager.getHero(heroKey())!!

                                            val ability = hero.abilities.values
                                                .flatMap { ability ->
                                                    ability.getAllProperties().map { property -> ability to property }
                                                }
                                                .firstOrNull { (ability, property) ->
                                                    property.internalKey == propertyKey() && ability.internalKey == abilityKey()
                                                }!!

                                            val player = this.source.playerOrThrow
                                            ability.second.addExperience(player.uuid, xpPoints())
                                            sendLevelInfo(player, player, ability.second, ability.first)
                                        }
                                    }
                                }
                                literal("info") {
                                    runs {
                                        val hero = HeroManager.getHero(heroKey())!!
                                        val ability = hero.abilities.values
                                            .flatMap { ability ->
                                                ability.getAllProperties().map { property -> ability to property }
                                            }
                                            .firstOrNull { (ability, property) ->
                                                property.internalKey == propertyKey() && ability.internalKey == abilityKey()
                                            }!!
                                        val player = this.source.playerOrThrow
                                        sendLevelInfo(player, player, ability.second, ability.first)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun <S> CommandContext<S>.getHeroInformation(): Triple<Hero<*>, AbstractAbility<*>, PlayerProperty<*>> {
        val hero = HeroManager.getHero(this.getArgument("hero", String::class.java))!!
        val propertyKey = this.getArgument("property", String::class.java)
        val abilityKey = this.getArgument("ability", String::class.java)

        val ability = hero.abilities.values
            .flatMap { ability ->
                ability.getAllProperties().map { property -> ability to property }
            }
            .firstOrNull { (ability, property) ->
                property.internalKey == propertyKey && ability.internalKey == abilityKey
            }!!

        return Triple(hero, ability.first, ability.second)
    }

    fun PlayerProperty<*>.toDebugText(): Text {
        return literalText {
            text(Text.translatable(this@toDebugText.translationKey))
            newLine()
            text("Max Level: ${this@toDebugText.maxLevel}")
            newLine()
            text("Base Value: ${this@toDebugText.baseValue}")
        }
    }

    fun PlayerEntity.sendDebugMessage(message: Text) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            sendMessage(message)
        }
    }

    fun sendLevelInfo(
        player: PlayerEntity,
        about: PlayerEntity,
        property: PlayerProperty<*>,
        ability: AbstractAbility<*>
    ) {
        val levelInfo = property.getLevelInfo(about.uuid)
        player.sendMessage(literalText {
            emptyLine()
            text("Level Info for ${property.name}") {
                underline = true
            }
            emptyLine()
            text("Player: ")
            text(player.name)
            text(" ${player.uuid}")
            newLine()
            text("Ability: ${ability.name}")
            newLine()
            text("Current Value: ${property.getValue(about.uuid)}")
            newLine()
            text("Current Level: ${levelInfo.currentLevel}/${levelInfo.maxLevel}")
            newLine()
            text("Next Level: ${levelInfo.nextLevel}")
            newLine()
            text("Step: ${levelInfo.experiencePoints}/${levelInfo.xpNextLevel}")
            newLine()
            text("Xp Needed for Upgrade: ${levelInfo.xpTillNextLevel}") { }
            newLine()
            text("Progress: ")
            text(
                getProgressBar(
                    levelInfo.percentageTillNextLevel,
                    100.0,
                    50,
                    "|".single()
                )
            )
            text(" ${String.format("%.3f", levelInfo.percentageTillNextLevel)}%")
        })
    }

    fun getProgressBar(
        current: Double,
        max: Double,
        totalBars: Int,
        symbol: Char,
        completedColor: Int = getProgressBarColor(current, max),
        notCompletedColor: Int = 0xa1a1a1
    ): Text {
        val percent = current.toFloat() / max
        val progressBars = (totalBars * percent).toInt()
        println("ProgressBars: $progressBars $totalBars $percent")

        return literalText {
            repeat(progressBars) {
                text("" + symbol) {
                    color = completedColor
                }
            }
            repeat(totalBars - progressBars) {
                text("" + symbol) {
                    color = notCompletedColor
                }
            }
        }
    }

    fun getProgressBarColor(progress: Double, maxProgress: Double): Int {
        val percentage = progress / maxProgress * 100.0
        return when {
            percentage > 66 -> 0x6fff36
            percentage > 30 -> 0xfff700
            else -> 0xff0000
        }
    }
}