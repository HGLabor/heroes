package gg.norisk.heroes.client.ui.components


import gg.norisk.heroes.client.ui.skilltree.HeroSelectorScreenV2
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.ui.components.ScalableButtonComponent
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.OverlayContainer
import io.wispforest.owo.ui.core.*
import io.wispforest.owo.ui.util.UISounds
import net.minecraft.client.MinecraftClient
import net.silkmc.silk.core.text.literal

class HeroListComponentV2(
    val heroes: List<Hero>,
    val heroSelectorScreen: HeroSelectorScreenV2,
    horizontalSizing: Sizing = Sizing.content(),
    verticalSizing: Sizing = Sizing.content()
) : FlowLayout(
    horizontalSizing, verticalSizing, Algorithm.VERTICAL
) {
    val lockInButton = ScalableButtonComponent("LOCK IN".literal, 1f, ::onLockInButton).apply {
        horizontalSizing(Sizing.fixed(100))
    }
    val editorButton = ScalableButtonComponent("EDITOR".literal, 1f, ::onEditorButton).apply {
        horizontalSizing(Sizing.fixed(48))
    }
    val lobbyButton = ScalableButtonComponent("SPEC".literal, 1f, ::onLobbyButton).apply {
        horizontalSizing(Sizing.fixed(48))
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

        val buttonWrapper = ButtonWrapper()
        child(buttonWrapper)

        buttonWrapper.child(lockInButton)
        if (heroSelectorScreen.isKitEditorEnabled) {
            buttonWrapper.child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                child(editorButton)
                child(lobbyButton)
                gap(5)
            })
        }
    }

    private inner class ButtonWrapper(
        horizontalSizing: Sizing = Sizing.content(),
        verticalSizing: Sizing = Sizing.content()
    ) : FlowLayout(
        horizontalSizing, verticalSizing, Algorithm.VERTICAL
    ) {
        init {
            gap(5)
        }
        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            if (heroSelectorScreen.adapter.children().any { it is OverlayContainer<*> }) {
                return
            }
            super.draw(context, mouseX, mouseY, partialTicks, delta)
        }
    }

    private fun onLockInButton(buttonComponent: ButtonComponent) {
        Networking.c2sHeroSelectorPacket.send(heroSelectorScreen.hero!!.internalKey)
    }

    private fun onEditorButton(buttonComponent: ButtonComponent) {
        Networking.c2sKitEditorRequestPacket.send(Unit)
    }

    private fun onLobbyButton(buttonComponent: ButtonComponent) {
        heroSelectorScreen.close()
    }

    override fun draw(context: OwoUIDrawContext?, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        lockInButton.active(heroSelectorScreen.hero != null)
        super.draw(context, mouseX, mouseY, partialTicks, delta)
    }

    inner class HeroHeadComponent(
        val hero: Hero, horizontalSizing: Sizing = Sizing.content(), verticalSizing: Sizing = Sizing.content()
    ) : FlowLayout(
        horizontalSizing, verticalSizing, Algorithm.HORIZONTAL
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
