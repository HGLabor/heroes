package gg.norisk.heroes.client.ui.skilltree

import gg.norisk.heroes.common.ability.CooldownProperty
import gg.norisk.heroes.common.ability.SingleUseProperty
import gg.norisk.heroes.common.command.DebugCommand.getProgressBar
import gg.norisk.heroes.common.hero.ability.AbstractAbility
import gg.norisk.heroes.common.hero.ability.SkillPropertyPacket
import gg.norisk.heroes.common.networking.Networking
import io.wispforest.owo.ui.core.Component
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.silkmc.silk.core.text.literalText

object SkillTreeUtils {
    fun toSkillTree(ability: AbstractAbility<*>): TreeNode<ISkill> {
        val root = TreeNode<ISkill>(object : ISkill {
            override fun isUnlocked(player: PlayerEntity): Boolean {
                return true
            }

            override fun isParentUnlocked(player: PlayerEntity): Boolean {
                return true
            }

            override fun title(): Text {
                return Text.translatable(ability.name)
            }

            override fun parent(): ISkill? {
                return null
            }

            override fun progress(player: PlayerEntity): Double {
                return 1.0
            }

            override fun skill() {

            }

            override fun isLast(): Boolean {
                return false
            }

            override fun tooltip(player: PlayerEntity): Text {
                return Text.empty()
            }

            override fun icon(): Component {
                return ability.getIconComponent()
            }
        })
        for (property in ability.getAllProperties()) {
            if (property is SingleUseProperty) continue
            if (property is CooldownProperty) {
                if (property.name == "NoCooldown") continue
            }
            var lastChild: TreeNode<ISkill>? = root

            repeat(property.maxLevel) { level ->
                val newChild = TreeNode<ISkill>(object : ISkill {
                    val parent = lastChild?.value
                    override fun isUnlocked(player: PlayerEntity): Boolean {
                        val levelInfo = property.getLevelInfo(player.uuid)
                        levelInfo.percentageTillNextLevel
                        return levelInfo.currentLevel > level
                    }

                    override fun isParentUnlocked(player: PlayerEntity): Boolean {
                        val levelInfo = property.getLevelInfo(player.uuid)
                        return levelInfo.currentLevel > level - 1
                    }

                    override fun title(): Text {
                        return Text.translatable(property.name + " " + intToRoman(level + 1))
                    }

                    override fun parent(): ISkill? {
                        return parent
                    }

                    override fun progress(player: PlayerEntity): Double {
                        val levelInfo = property.getLevelInfo(player.uuid)
                        if (levelInfo.currentLevel == level) {
                            return levelInfo.percentageTillNextLevel / 100.0
                        } else if (levelInfo.currentLevel > level) {
                            return 1.0
                        } else {
                            return 0.0
                        }
                    }

                    override fun skill() {
                        Networking.c2sSkillProperty.send(
                            SkillPropertyPacket(
                                ability.hero.internalKey, ability.internalKey, property.internalKey
                            )
                        )
                    }

                    override fun isLast(): Boolean {
                        return level >= property.maxLevel - 1
                    }

                    private fun <T> getValueText(value: T): Text {
                        return literalText {
                            text(value.toString())
                            if (property is CooldownProperty) {
                                text("s")
                            }
                        }
                    }

                    override fun tooltip(player: PlayerEntity): Text {
                        val levelInfo = property.getLevelInfo(player.uuid, level)
                        return literalText {
                            text("[")
                            text(Text.translatable(property.translationKey))
                            text("]")
                            newLine()
                            text(Text.translatable(property.descriptionKey))
                            emptyLine()
                            text("[")
                            text("Progress")
                            text("]")
                            newLine()
                            text(
                                getProgressBar(
                                    levelInfo.percentageTillNextLevel,
                                    100.0,
                                    50,
                                    "|".single()
                                )
                            )
                            text(" ${String.format("%.2f", levelInfo.percentageTillNextLevel)}%")
                            emptyLine()
                            text(getValueText(property.getValue(level + 1)))
                        }
                    }

                    override fun icon(): Component {
                        return property.icon.invoke()
                    }
                })
                lastChild?.addChild(newChild)
                lastChild = newChild
            }
        }

        return root
    }

    private val m_k = listOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
    private val m_v = listOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")

    fun intToRoman(num: Int): String {
        var str = ""
        var n = num

        for (i in m_k.indices) {
            while (n >= m_k[i]) {
                n -= m_k[i]
                str += m_v[i]
            }
        }
        return str
    }
}