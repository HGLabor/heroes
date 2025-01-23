package gg.norisk.heroes.common.command

import com.mojang.brigadier.arguments.StringArgumentType
import gg.norisk.heroes.common.ability.AbstractNumberProperty
import gg.norisk.heroes.common.ability.PlayerProperty
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.ability.operation.MultiplyBase
import gg.norisk.heroes.common.command.DebugCommand.getHeroInformation
import gg.norisk.heroes.server.config.ConfigManagerServer
import net.minecraft.server.command.ServerCommandSource
import net.silkmc.silk.commands.ArgumentCommandBuilder
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.core.text.broadcastText
import java.awt.Color

object EditPropertyCommand {
    fun ArgumentCommandBuilder<ServerCommandSource, String>.editCommand() {
        literal("edit") {
            baseValue()
            maxLevel()
            levelScale()
            operation()
        }
    }

    private fun LiteralCommandBuilder<ServerCommandSource>.operation() {
        literal("operation") {
            literal("type") {
                argument<String>("type") {  typeString ->
                    suggestList {
                        val type = (it.getHeroInformation().third as AbstractNumberProperty).modifier::class.simpleName
                        val set = mutableSetOf(type, MultiplyBase::class.simpleName, AddValueTotal::class.simpleName)
                        set.toList()
                    }
                    runs {
                        val (hero, ability, property) = this.getHeroInformation()
                        val oldModifier = (property as? AbstractNumberProperty?)?.modifier
                        val source = this.source.displayName
                        val newValue = when (oldModifier) {
                            is AddValueTotal -> {
                                oldModifier.steps
                            }

                            is MultiplyBase -> {
                                oldModifier.steps
                            }

                            null -> TODO()
                        }
                        when(typeString()) {
                            AddValueTotal::class.simpleName -> {
                                (property as? AbstractNumberProperty?)?.modifier = AddValueTotal(newValue)
                            }
                            MultiplyBase::class.simpleName -> {
                                (property as? AbstractNumberProperty?)?.modifier = MultiplyBase(newValue)
                            }
                        }
                        runCatching {
                            hero.save()
                            hero.load()
                        }.onSuccess {
                            this.source.server.broadcastText {
                                text(source)
                                text(" changed ${hero.name}, ${ability.name}, ${property.name}, modifier")
                                newLine()
                                text("- ${oldModifier::class.simpleName}") {
                                    color = Color.RED.rgb
                                }
                                newLine()
                                text("+ ${(property as? AbstractNumberProperty?)!!.modifier::class.simpleName}") {
                                    color = Color.GREEN.rgb
                                }
                            }
                        }.onFailure {
                            this.source.server.broadcastText {
                                text(source)
                                text(" changed ${hero.name}, ${ability.name}, ${property.name}, modifier")
                                newLine()
                                text("ERROR ${it.message}")
                            }
                            it.printStackTrace()
                        }.also {
                            ConfigManagerServer.sendHeroSettings(hero)
                        }
                    }
                }
            }
            literal("list") {
                argument<String>("values", StringArgumentType.string()) { valuesString ->
                    suggestList {
                        when (val modifier = (it.getHeroInformation().third as? AbstractNumberProperty?)?.modifier) {
                            is AddValueTotal -> listOf("\"${modifier.steps}\"")
                            is MultiplyBase -> listOf("\"${modifier.steps}\"")
                            else -> listOf("\"SCHREIB NORISK AN\"")
                        }
                    }
                    runs {
                        val (hero, ability, property) = this.getHeroInformation()
                        val modifier = (property as? AbstractNumberProperty?)?.modifier
                        val possibleList = valuesString()
                        var oldValue: Any? = null
                        val source = this.source.displayName
                        when (modifier) {
                            is AddValueTotal -> {
                                oldValue = modifier.steps
                                modifier.steps = PlayerProperty.JSON.decodeFromString<List<Double>>(possibleList)
                            }

                            is MultiplyBase -> {
                                oldValue = modifier.steps
                                modifier.steps = PlayerProperty.JSON.decodeFromString<List<Double>>(possibleList)
                            }

                            null -> TODO()
                        }

                        runCatching {
                            hero.save()
                            hero.load()
                        }.onSuccess {
                            this.source.server.broadcastText {
                                text(source)
                                text(" changed ${hero.name}, ${ability.name}, ${property.name}, modifier")
                                newLine()

                                val newValue = when (modifier) {
                                    is AddValueTotal -> {
                                        modifier.steps
                                    }

                                    is MultiplyBase -> {
                                        modifier.steps
                                    }
                                }

                                text("- $oldValue") {
                                    color = Color.RED.rgb
                                }
                                newLine()
                                text("+ $newValue") {
                                    color = Color.GREEN.rgb
                                }
                            }
                        }.onFailure {
                            this.source.server.broadcastText {
                                text(source)
                                text(" changed ${hero.name}, ${ability.name}, ${property.name}, modifier")
                                newLine()
                                text("ERROR ${it.message}")
                            }
                            it.printStackTrace()
                        }.also {
                            ConfigManagerServer.sendHeroSettings(hero)
                        }
                    }
                }
            }
        }
    }

    private fun LiteralCommandBuilder<ServerCommandSource>.levelScale() {
        literal("levelScale") {
            argument<Int>("value") { value ->
                suggestList {
                    listOf(it.getHeroInformation().third.levelScale)
                }
                runs {
                    val (hero, ability, property) = this.getHeroInformation()
                    if (property is AbstractNumberProperty) {
                        val oldValue = property.levelScale
                        property.levelScale = value()
                        val source = this.source.displayName
                        runCatching {
                            hero.save()
                            hero.load()
                        }.onSuccess {
                            this.source.server.broadcastText {
                                text(source)
                                text(" changed ${hero.name}, ${ability.name}, ${property.name}, levelScale")
                                newLine()
                                text("from $oldValue to ${property.baseValue}")
                            }
                        }.onFailure {
                            this.source.server.broadcastText {
                                text(source)
                                text(" changed ${hero.name}, ${ability.name}, ${property.name}, levelScale")
                                newLine()
                                text("ERROR ${it.message}")
                            }
                            it.printStackTrace()
                        }.also {
                            ConfigManagerServer.sendHeroSettings(hero)
                        }
                    }
                }
            }
        }
    }


    private fun LiteralCommandBuilder<ServerCommandSource>.maxLevel() {
        literal("maxLevel") {
            argument<Int>("value") { value ->
                suggestList {
                    listOf(it.getHeroInformation().third.maxLevel)
                }
                runs {
                    val (hero, ability, property) = this.getHeroInformation()
                    if (property is AbstractNumberProperty) {
                        val oldValue = property.maxLevel
                        property.maxLevel = value()
                        val source = this.source.displayName
                        runCatching {
                            hero.save()
                            hero.load()
                        }.onSuccess {
                            this.source.server.broadcastText {
                                text(source)
                                text(" changed ${hero.name}, ${ability.name}, ${property.name}, maxLevel")
                                newLine()
                                text("from $oldValue to ${property.baseValue}")
                            }
                        }.onFailure {
                            this.source.server.broadcastText {
                                text(source)
                                text(" changed ${hero.name}, ${ability.name}, ${property.name}, maxLevel")
                                newLine()
                                text("ERROR ${it.message}")
                            }
                            it.printStackTrace()
                        }.also {
                            ConfigManagerServer.sendHeroSettings(hero)
                        }
                    }
                }
            }
        }
    }

    private fun LiteralCommandBuilder<ServerCommandSource>.baseValue() {
        literal("baseValue") {
            argument<String>("value") { value ->
                suggestList {
                    listOf(it.getHeroInformation().third.baseValue)
                }
                runs {
                    val (hero, ability, property) = this.getHeroInformation()
                    if (property is AbstractNumberProperty) {
                        val oldValue = property.baseValue
                        property.baseValue = value().toDouble()
                        val source = this.source.displayName
                        runCatching {
                            hero.save()
                            hero.load()
                        }.onSuccess {
                            this.source.server.broadcastText {
                                text(source)
                                text(" changed ${hero.name}, ${ability.name}, ${property.name}, baseValue")
                                newLine()
                                text("from $oldValue to ${property.baseValue}")
                            }
                        }.onFailure {
                            this.source.server.broadcastText {
                                text(source)
                                text(" changed ${hero.name}, ${ability.name}, ${property.name}, baseValue")
                                newLine()
                                text("ERROR ${it.message}")
                            }
                            it.printStackTrace()
                        }.also {
                            ConfigManagerServer.sendHeroSettings(hero)
                        }
                    }
                }
            }
        }
    }
}