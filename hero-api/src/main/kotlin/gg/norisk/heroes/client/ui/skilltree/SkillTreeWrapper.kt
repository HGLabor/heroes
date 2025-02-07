package gg.norisk.heroes.client.ui.skilltree

import gg.norisk.heroes.common.HeroesManager.toId
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.ability.AbstractAbility
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.TextureComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import io.wispforest.owo.ui.util.UISounds
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText

class SkillTreeWrapper(
    val hero: Hero,
    horizontalSizing: Sizing = Sizing.content(),
    verticalSizing: Sizing = Sizing.content()
) : FlowLayout(horizontalSizing, verticalSizing, Algorithm.VERTICAL) {
    val skillTreeWrapper = Containers.horizontalFlow(Sizing.content(), Sizing.content())
    val tabs = mutableListOf<TabButton>()

    init {
        var index = 0
        for ((name, ability) in hero.abilities) {
            tabs += TabButton(ability, index)
            index++
        }

        child(TabWrapper().apply {
            children(tabs)
            //zIndex(5000)
            allowOverflow(true)
            gap(2)
        })
        child(skillTreeWrapper)

        tabs.first { it.ability.hasUnlocked(MinecraftClient.getInstance().player!!) }.apply {
            this.isSelected = true
            this.onClick()
        }

        //children(tabs)
    }

    override fun drawChildren(
        context: OwoUIDrawContext,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        delta: Float,
        children: MutableList<out Component>
    ) {
        super.drawChildren(context, mouseX, mouseY, partialTicks, delta, children.reversed())
    }

    override fun draw(context: OwoUIDrawContext?, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        super.draw(context, mouseX, mouseY, partialTicks, delta)
    }

    inner class TabWrapper(
        horizontalSizing: Sizing = Sizing.content(),
        verticalSizing: Sizing = Sizing.content()
    ) : FlowLayout(horizontalSizing, verticalSizing, Algorithm.HORIZONTAL) {
    }

    inner class TabButton(
        val ability: AbstractAbility<*>,
        index: Int,
        horizontalSizing: Sizing = Sizing.fixed(28),
        verticalSizing: Sizing = Sizing.fixed(28)
    ) : FlowLayout(horizontalSizing, verticalSizing, Algorithm.VERTICAL) {
        var page = AbilitySkillTreeComponent(ability)
        var isSelected = false
        var item = ability.getIconComponent().id("unlocked")
        var lockIcon = Components.texture("textures/gui/lock_icon.png".toId(), 0, 0, 20, 20, 20, 20).apply {
            id("locked")
            tooltip(literalText {
                text(ability.name)
                newLine()
                text(ability.getUnlockCondition()) {
                    color = Colors.LIGHT_GRAY
                }
            })
        }

        init {
            surface { context, container ->
                context.matrices.push()
                val texture = if (isSelected) {
                    if (index == 0) {
                        Identifier.of("textures/gui/sprites/advancements/tab_above_left_selected.png")
                    } else {
                        Identifier.of("textures/gui/sprites/advancements/tab_above_middle_selected.png")
                    }
                } else {
                    if (index == 0) {
                        Identifier.of("textures/gui/sprites/advancements/tab_above_left.png")
                    } else {
                        Identifier.of("textures/gui/sprites/advancements/tab_above_middle.png")
                    }
                }
                context.drawTexture(
                    texture,
                    container.x(),
                    container.y(),
                    0f,
                    0f,
                    28,
                    32,
                    28,
                    32
                )
                // NinePatchTexture.draw(OwoUIDrawContext.PANEL_NINE_PATCH_TEXTURE, context, container.x(), container.y(), container.width(), container.height())
                context.matrices.pop()
            }
            mouseDown().subscribe { _, _, _ ->
                if (!ability.hasUnlocked(MinecraftClient.getInstance().player!!)) {
                    MinecraftClient.getInstance().soundManager.play(
                        PositionedSoundInstance.master(
                            SoundEvents.ENTITY_VILLAGER_NO,
                            1.0f
                        )
                    )
                    return@subscribe true
                }
                if (!isSelected) {
                    UISounds.playInteractionSound()
                    isSelected = !isSelected
                    tabs.filter { it != this }.forEach { it.isSelected = false }
                    onClick()
                }
                return@subscribe true
            }
            tooltip(ability.name.literal)
            alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
            //positioning(Positioning.absolute(0, 0))
            child(lockIcon)
            //zIndex(-5000)
            allowOverflow(true)
        }

        fun onClick() {
            if (isSelected) {
                skillTreeWrapper.clearChildren()
                skillTreeWrapper.child(page)
            }
        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            val hasUnlocked = ability.hasUnlocked(MinecraftClient.getInstance().player!!)
            val locked = childById(TextureComponent::class.java, "locked")
            val unlocked = childById(Component::class.java, "unlocked")
            val component = if (hasUnlocked) {
                if (unlocked == null) {
                    child(item)
                }
                if (locked != null) {
                    removeChild(locked)
                }
                item
            } else {
                if (locked == null) {
                    child(lockIcon)
                }
                if (unlocked != null) {
                    removeChild(item)
                }
                lockIcon
            }


            if (isSelected) {
                component.margins(Insets.none())
            } else {
                component.margins(Insets.top(8))
            }
            super.draw(context, mouseX, mouseY, partialTicks, delta)
        }
    }
}