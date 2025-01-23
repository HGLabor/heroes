package gg.norisk.heroes.client.ui.components


import gg.norisk.heroes.client.ui.screen.HeroSelectorScreen
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.ui.components.ScalableButtonComponent
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import io.wispforest.owo.ui.util.UISounds
import net.silkmc.silk.core.text.literal

class HeroListComponent(
    val heroes: List<Hero<*>>,
    val heroSelectorScreen: HeroSelectorScreen,
    horizontalSizing: Sizing = Sizing.content(),
    verticalSizing: Sizing = Sizing.content()
) :
    FlowLayout(
        horizontalSizing,
        verticalSizing,
        Algorithm.VERTICAL
    ) {
    val lockInButton = ScalableButtonComponent("LOCK IN".literal, 1f, ::onLockInButton).apply {
        horizontalSizing(Sizing.fixed(100))
    }
    val editorButton = ScalableButtonComponent("EDITOR".literal, 1f, ::onEditorButton).apply {
        horizontalSizing(Sizing.fixed(100))
    }

    init {
        gap(5)
        alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)

        val grid = Containers.grid(Sizing.content(), Sizing.content(), 1, heroes.size)
        for ((index, hero) in heroes.withIndex()) {
            grid.child(HeroHeadComponent(hero), 0, index)
        }
        child(grid)
        grid.surface(Surface.VANILLA_TRANSLUCENT)
        grid.padding(Insets.of(5))

        child(lockInButton)
        if (heroSelectorScreen.isKitEditorEnabled) {
            child(editorButton)
        }
    }

    private fun onLockInButton(buttonComponent: ButtonComponent) {
        Networking.c2sHeroSelectorPacket.send(heroSelectorScreen.hero!!.internalKey)
    }

    private fun onEditorButton(buttonComponent: ButtonComponent) {
        Networking.c2sKitEditorRequestPacket.send(Unit)
    }

    override fun draw(context: OwoUIDrawContext?, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        lockInButton.active(heroSelectorScreen.hero != null)
        super.draw(context, mouseX, mouseY, partialTicks, delta)
    }

    inner class HeroHeadComponent(
        val hero: Hero<*>,
        horizontalSizing: Sizing = Sizing.content(),
        verticalSizing: Sizing = Sizing.content()
    ) : FlowLayout(
        horizontalSizing,
        verticalSizing,
        Algorithm.HORIZONTAL
    ) {
        init {
            val l = 8
            val m = 8
            val heroHead = Components.texture(hero.icon, 0, 0, 64, 64, 64, 64)
            //val heroHead2 = Components.texture(hero.skin, 40, l, 8, m, 64, 64)
            //OVERLAY
            heroHead.sizing(Sizing.fixed(32))
            child(heroHead)

            margins(Insets.of(2))
            padding(Insets.of(2))

            surface(Surface.outline(java.awt.Color.WHITE.darker().rgb))

            mouseDown().subscribe { _, _, _ ->
                UISounds.playButtonSound()
                heroSelectorScreen.hero = hero
                return@subscribe true
            }
            mouseEnter().subscribe {
                surface(Surface.outline(Color.WHITE.argb()))
            }
            mouseLeave().subscribe {
                surface(Surface.outline(java.awt.Color.WHITE.darker().rgb))
            }
        }

        override fun draw(context: OwoUIDrawContext?, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            super.draw(context, mouseX, mouseY, partialTicks, delta)
        }
    }
}
