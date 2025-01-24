package gg.norisk.heroes.client.ui.components

import gg.norisk.heroes.common.ability.CooldownProperty
import gg.norisk.heroes.common.ability.LevelInformation
import gg.norisk.heroes.common.ability.PlayerProperty
import gg.norisk.heroes.common.ability.SingleUseProperty
import gg.norisk.heroes.common.hero.ability.AbstractAbility
import gg.norisk.heroes.common.hero.ability.SkillPropertyPacket
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.ui.components.ScalableButtonComponent
import gg.norisk.ui.components.ScalableLabelComponent
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText
import java.awt.Color
import java.util.*

class AbilityComponent(
    val ability: AbstractAbility<*>,
    val uuid: UUID = MinecraftClient.getInstance().player!!.uuid,
    horizontalSizing: Sizing = Sizing.content(),
    verticalSizing: Sizing = Sizing.content()
) : FlowLayout(horizontalSizing, verticalSizing, Algorithm.HORIZONTAL) {

    val abilityDescription = ScalableLabelComponent(ability.description, 0.5f).apply {
        shadow(true)
    }
    val leftWrapper = Containers.verticalFlow(Sizing.fill(45), Sizing.content())

    init {
        surface(Surface.VANILLA_TRANSLUCENT)
        padding(Insets.of(5))
        leftWrapper.apply {
            child(ScalableLabelComponent(literalText {
                text(ability.name.literal)
                underline = true
                bold = true
            }).apply {
                shadow(true)
            })
            child(abilityDescription)
            gap(3)
        }
        child(leftWrapper)

        gap(2)

        horizontalAlignment(HorizontalAlignment.LEFT)

        child(Containers.verticalFlow(Sizing.fill(55), Sizing.content()).apply {
            //debug()
            gap(3)
            for (property in ability.getAllProperties()) {
                if (property is SingleUseProperty) continue
                child(PropertyComponent(property))
            }
        })
    }

    override fun draw(context: OwoUIDrawContext?, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        abilityDescription.maxWidth(leftWrapper.fullSize().width * 2)
        super.draw(context, mouseX, mouseY, partialTicks, delta)
    }

    private inner class PropertyComponent(
        val property: PlayerProperty<*>,
        horizontalSizing: Sizing = Sizing.fill(),
        verticalSizing: Sizing = Sizing.content()
    ) : FlowLayout(horizontalSizing, verticalSizing, Algorithm.VERTICAL) {
        val progressBar = Containers.horizontalFlow(Sizing.fill(83), Sizing.fixed(3))
        var progressColor: Color = Color.GREEN
        val title = ScalableLabelComponent(literalText {
            text(Text.translatable(property.name)) {
                bold = true
            }
            text(":")
        }, 0.75f).apply {
            shadow(true)
        }
        val valueLabel = ScalableLabelComponent(getValueText(property.getValue(uuid)), 0.75f).apply {
            shadow(true)
        }
        val levelLabel = ScalableLabelComponent(getLevelText(property.getLevelInfo(uuid)), 0.5f).apply {
            shadow(true)
        }
        val skillButton = ScalableButtonComponent("+".literal, 0.75f, ::onSkill).apply {
            sizing(Sizing.fixed(15))
        }

        init {
            //debug()
            child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                child(title)
                child(valueLabel)
                gap(5)
                tooltip(upgradeTooltip())
                //debug()
            })
            child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                    alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    child(levelLabel)
                    child(progressBar)
                    gap(3)
                })
                child(skillButton)
                alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                gap(5)
                //debug()
            })
            gap(3)
            //alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP)
        }

        private fun upgradeTooltip(): Text {
            return literalText {
                text("Upgrade") {
                    bold = true
                }
                repeat(property.maxLevel + 1) { level ->
                    newLine()
                    text("Lvl $level -> ")
                    text(getValueText(property.getValue(level)))
                }
            }
        }

        private fun progressTooltip(levelInformation: LevelInformation): Text {
            return literalText {
                text((levelInformation.xpNextLevel - levelInformation.xpTillNextLevel).toString())
                text("/")
                text(levelInformation.xpNextLevel.toString())
            }
        }

        private fun onSkill(buttonComponent: ButtonComponent) {
            mcCoroutineTask(client = true, sync = true) {
                Networking.c2sSkillProperty.send(
                    SkillPropertyPacket(
                        ability.hero.internalKey, ability.internalKey, property.internalKey
                    )
                )
            }
        }

        private fun getLevelText(levelInformation: LevelInformation): Text {
            return literalText {
                text("Lvl ")
                text(levelInformation.currentLevel.toString())
                text("/")
                text(levelInformation.maxLevel.toString())
            }
        }

        private fun <T> getValueText(value: T): Text {
            return literalText {
                text(value.toString())
                if (property is CooldownProperty) {
                    text("s")
                }
            }
        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            super.draw(context, mouseX, mouseY, partialTicks, delta)
            val levelInfo = property.getLevelInfo(MinecraftClient.getInstance().session.uuidOrNull)
            valueLabel.text(getValueText(property.getValue(uuid)))
            levelLabel.text(getLevelText(levelInfo))
            progressBar.tooltip(progressTooltip(levelInfo))
            levelLabel.tooltip(progressTooltip(levelInfo))
            val currentPercentage = levelInfo.percentageTillNextLevel
            progressBar.surface { surfaceContext, component ->
                val barWidth = progressBar.width() * (currentPercentage / 100.0)

                surfaceContext.fill(
                    RenderLayer.getGui(),
                    component.x(),
                    component.y(),
                    (component.x() + barWidth).toInt(),
                    component.y() + component.height(),
                    0,
                    progressColor.rgb
                )

                surfaceContext.fill(
                    RenderLayer.getGui(),
                    component.x() + barWidth.toInt(),
                    component.y(),
                    (component.x() + progressBar.width()),
                    component.y() + component.height(),
                    0,
                    progressColor.darker().darker().withAlpha(200).rgb
                )
            }
        }
    }

    fun Color.withAlpha(alpha: Int): Color {
        return Color(red, green, blue, alpha)
    }
}