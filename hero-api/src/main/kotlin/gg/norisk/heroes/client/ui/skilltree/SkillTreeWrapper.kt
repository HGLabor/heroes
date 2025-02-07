package gg.norisk.heroes.client.ui.skilltree

import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.ability.AbstractAbility
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import io.wispforest.owo.ui.util.UISounds
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import net.silkmc.silk.core.text.literal

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

        tabs.first().apply {
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
        ability: AbstractAbility<*>,
        index: Int,
        horizontalSizing: Sizing = Sizing.fixed(28),
        verticalSizing: Sizing = Sizing.fixed(28)
    ) : FlowLayout(horizontalSizing, verticalSizing, Algorithm.VERTICAL) {
        var page = AbilitySkillTreeComponent(ability)
        var isSelected = false
        var item = ability.getIconComponent()

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
            child(item)
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
            if (isSelected) {
                item.margins(Insets.none())
            } else {
                item.margins(Insets.top(8))
            }
            super.draw(context, mouseX, mouseY, partialTicks, delta)
        }
    }
}