package gg.norisk.heroes.client.ui.skilltree


//import me.cortex.nvidium.Nvidium
import gg.norisk.heroes.common.hero.HeroManager
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.client.gui.DrawContext

class SkillTreeScreen : BaseOwoScreen<FlowLayout>() {

    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }


    override fun build(root: FlowLayout) {
        root.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
        root.child(SkillTreeWrapper(HeroManager.getHero("katara")!!))
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
    }
}
