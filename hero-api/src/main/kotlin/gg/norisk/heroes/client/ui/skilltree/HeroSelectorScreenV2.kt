package gg.norisk.heroes.client.ui.skilltree


//import me.cortex.nvidium.Nvidium
import gg.norisk.heroes.client.ui.components.HeroListComponentV2
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.player.ffaPlayer
import gg.norisk.ui.components.LabelButtonComponent
import gg.norisk.ui.components.ScalableLabelComponent
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import io.wispforest.owo.ui.util.UISounds
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText
import java.awt.Color

class HeroSelectorScreenV2(val heroes: List<Hero>, val isKitEditorEnabled: Boolean = false) :
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
    var centerLabel: FlowLayout = Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
        child(ScalableLabelComponent(literalText {
            text("CHOOSE YOUR HERO")
        }, 3f).apply {
            shadow(true)
        })

        alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
        positioning(Positioning.relative(30, 40))
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    val adapter get() = uiAdapter.rootComponent

    private fun heroAbility(hero: Hero): FlowLayout {
        val container = Containers.verticalFlow(Sizing.content(), Sizing.content())
            .apply { positioning(Positioning.relative(50, 30)) }

        container.child(ScalableLabelComponent(literalText {
            text(hero.name) {
            }
        }, 3f).apply {
            shadow(true)
        })
        container.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
        container.child(LabelButtonComponent("SKILL TREE".literal, Color.YELLOW).apply {
            label.scale = 1.5f
            mouseDown().subscribe { _, _, _ ->
                UISounds.playButtonSound()
                buildSkillTree(hero)
                return@subscribe true
            }
        })
        return container
    }

    private fun buildSkillTree(hero: Hero) {
        uiAdapter.rootComponent.child(Containers.overlay(SkillTreeWrapper(hero)))
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
        val heroList = HeroListComponentV2(heroes, this)
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
