package gg.norisk.heroes.client.ui.components

import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.ability.AbstractAbility
import gg.norisk.ui.components.ScalableButtonComponent
import gg.norisk.ui.components.ScalableLabelComponent
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.client.MinecraftClient
import net.silkmc.silk.core.text.literal
import java.util.*

class AbilitiesComponent(
    val hero: Hero<*>,
    val uuid: UUID = MinecraftClient.getInstance().player!!.uuid,
    horizontalSizing: Sizing = Sizing.content(),
    verticalSizing: Sizing = Sizing.content()
) : FlowLayout(horizontalSizing, verticalSizing, Algorithm.VERTICAL) {

    val mainWrapper = Containers.horizontalFlow(Sizing.content(), Sizing.content())
    val buttonWrapper = Containers.horizontalFlow(Sizing.content(), Sizing.content())

    init {
        for (ability in hero.abilities.values) {
            buttonWrapper.child(ScalableButtonComponent(ability.name.literal, 0.8f) {
                onAbilityButtonClick(it, ability)
            })
        }

        child(buttonWrapper)
        child(mainWrapper)
        buttonWrapper.children().filterIsInstance<ButtonComponent>().first().onPress()
    }

    override fun draw(context: OwoUIDrawContext?, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        for (child in mainWrapper.children()) {
            val width = buttonWrapper.fullSize().width
            child.horizontalSizing(Sizing.fixed(width))
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta)
    }

    private fun onAbilityButtonClick(it: ButtonComponent, ability: AbstractAbility<*>) {
        buttonWrapper.children().filterIsInstance<ButtonComponent>().forEach { button ->
            button.active(true)
        }
        it.active(false)
        mainWrapper.clearChildren()
        mainWrapper.child(AbilityComponent(ability))
    }
}