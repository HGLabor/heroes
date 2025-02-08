package gg.norisk.heroes.common.ability

import gg.norisk.heroes.common.ability.operation.Operation
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.core.Component
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.item.Items

@Serializable
class SingleUseProperty(
    override var baseValue: Double,
    override var maxLevel: Int,
    override var name: String,
    override var modifier: Operation,
    @Transient
    override val icon:  () -> Component = { Components.item(Items.CLOCK.defaultStack) },
    override var levelScale: Int = 10
) : AbstractUsageProperty() {

}