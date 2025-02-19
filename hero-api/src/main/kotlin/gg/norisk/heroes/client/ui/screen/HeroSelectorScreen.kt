package gg.norisk.heroes.client.ui.screen


//import me.cortex.nvidium.Nvidium
import gg.norisk.heroes.client.ui.components.AbilitiesComponent
import gg.norisk.heroes.client.ui.components.HeroListComponent
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.player.ffaPlayer
import gg.norisk.ui.components.ScalableLabelComponent
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText

class HeroSelectorScreen(val heroes: List<Hero>, val isKitEditorEnabled: Boolean = false) :
    BaseOwoScreen<FlowLayout>() {
    var hero: Hero? = null
        set(value) {
            heroInfoComponent?.remove()
            centerLabel.remove()
            if (field != value) {
                field = value
                if (value != null) {
                    heroInfoComponent = heroAbility(value)
                    this.uiAdapter.rootComponent.child(heroInfoComponent)
                }
            } else {
                field = null
                this.uiAdapter.rootComponent.child(centerLabel)
            }
        }
    var heroInfoComponent: FlowLayout? = null
    var centerLabel: FlowLayout = Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
        child(ScalableLabelComponent(literalText {
            text("CHOOSE YOUR HERO")
        }, 3f).apply {
            shadow(true)
        })
        alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
        positioning(Positioning.relative(30, 50))
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    private fun heroAbility(hero: Hero): FlowLayout {
        val container = Containers.verticalFlow(Sizing.content(), Sizing.content())
            .apply { positioning(Positioning.relative(0, 30)) }
        container.child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
            child(ScalableLabelComponent(literalText {
                text(hero.name.uppercase())
                bold = true
                //color = Color.YELLOW.rgb
            }, 2f).apply {
                this.margins(Insets.of(3))
            })
            child(XpLabel(1f).apply {
                this.margins(Insets.of(3))
            })
            gap(3)
        })
        container.gap(5)
        container.child(AbilitiesComponent(hero))
        return container
    }

    private class XpLabel(scale: Float = 1f) : ScalableLabelComponent("".literal, scale) {
        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            text(literalText {
                text("XP: ")
                text((MinecraftClient.getInstance().player?.ffaPlayer?.xp ?: 0).toString())
            })
            super.draw(context, mouseX, mouseY, partialTicks, delta)
        }
    }

    override fun close() {
        super.close()
        if (FabricLoader.getInstance().isModLoaded("nvidium")) {
            //Nvidium.FORCE_DISABLE = false
            this.client?.worldRenderer?.reload()
        }
    }

    override fun build(root: FlowLayout) {
        val heroList = HeroListComponent(heroes, this)
        heroList.positioning(Positioning.relative(50, 90))

        root.child(heroList)
        if (hero == null) {
            root.child(centerLabel)
        }

        if (FabricLoader.getInstance().isModLoaded("nvidium")) {
            //Nvidium.FORCE_DISABLE = true
            this.client?.worldRenderer?.reload()
        }
    }

    override fun shouldPause(): Boolean {
        return false
    }

    override fun shouldCloseOnEsc(): Boolean {
        return FabricLoader.getInstance().isDevelopmentEnvironment
    }
}
