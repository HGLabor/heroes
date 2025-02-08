package gg.norisk.heroes.client.ui.skilltree

import com.mojang.blaze3d.systems.RenderSystem
import gg.norisk.heroes.common.HeroesManager.toId
import gg.norisk.heroes.common.hero.ability.AbstractAbility
import gg.norisk.heroes.common.player.dbPlayer
import gg.norisk.heroes.common.ui.ScrollContainerV2
import gg.norisk.ui.components.ScalableLabelComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import io.wispforest.owo.ui.util.NinePatchTexture
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat.DrawMode
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText
import org.joml.Matrix4f
import org.joml.Vector2d
import kotlin.math.cos
import kotlin.math.sin


class AbilitySkillTreeComponent(
    val ability: AbstractAbility<*>,
    horizontalSizing: Sizing = Sizing.fill(50),
    verticalSizing: Sizing = Sizing.fill(60),
    val player: PlayerEntity = MinecraftClient.getInstance().player!!
) : FlowLayout(horizontalSizing, verticalSizing, Algorithm.VERTICAL) {
    val shadow = "textures/gui/shadow.png".toId()
    val scrollChild = ScrollChild()
    val scroll = ScrollContainerV2(Sizing.fill(), Sizing.fill(80), scrollChild)
    var isHovered = false

    init {
        surface(Surface.PANEL)
        alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP)
        padding(Insets.of(5).withLeft(8).withRight(8))

        //val scroll = Containers.horizontalScroll(Sizing.fill(25), Sizing.fill(60), scrollChild)
        child(Containers.horizontalFlow(Sizing.fill(), Sizing.content(1)).apply {
            child(ScalableLabelComponent(literalText {
                text(ability.name.literal)
                color = Colors.GRAY
            }))
        })
        child(Containers.horizontalFlow(Sizing.fill(), Sizing.content(1)).apply {
            child(ScalableLabelComponent(literalText {
                text(ability.description)
                color = Colors.GRAY
            },0.75f).apply {
                maxWidth(350)
            })
        })
        child(scroll)
        scroll.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
        scroll.padding(Insets.of(2))
        scrollChild.surface(Surface { context, component ->
            val divider = 240f
            RenderSystem.setShaderColor(divider / 255f, divider / 255f, divider / 255f, 1f);
            context.drawTexture(
                ability.getBackgroundTexture(),
                component.x(),
                component.y(),
                0f,
                0f,
                component.width(),
                component.height(),
                16,
                16
            );

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        })

        scrollChild.alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP)
        scrollChild.child(getComponent(SkillTreeUtils.toSkillTree(ability)))
        child(XPComponent())
    }

    inner class XPComponent(
        horizontalSizing: Sizing = Sizing.content(),
        verticalSizing: Sizing = Sizing.content(),
    ) : FlowLayout(horizontalSizing, verticalSizing, Algorithm.VERTICAL) {

        init {

        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            super.draw(context, mouseX, mouseY, partialTicks, delta)
            val textRenderer = MinecraftClient.getInstance().textRenderer
            val string = "" + player.dbPlayer.xp

            val x = this.x() - textRenderer.getWidth(string) / 2
            val y = this.y() + textRenderer.fontHeight / 2

            context.drawText(textRenderer, string, x + 1, y, 0, false)
            context.drawText(textRenderer, string, x - 1, y, 0, false)
            context.drawText(textRenderer, string, x, y + 1, 0, false)
            context.drawText(textRenderer, string, x, y - 1, 0, false)
            context.drawText(textRenderer, string, x, y, 8453920, false)
        }
    }


    inner class ScrollChild(
        horizontalSizing: Sizing = Sizing.fixed(500),
        verticalSizing: Sizing = Sizing.fixed(500),
    ) : FlowLayout(horizontalSizing, verticalSizing, Algorithm.VERTICAL) {
        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            super.draw(context, mouseX, mouseY, partialTicks, delta)
        }

        override fun drawChildren(
            context: OwoUIDrawContext,
            mouseX: Int,
            mouseY: Int,
            partialTicks: Float,
            delta: Float,
            children: MutableList<out Component>
        ) {
            super.drawChildren(context, mouseX, mouseY, partialTicks, delta, children)
        }
    }

    private fun getComponent(node: TreeNode<ISkill>): Component {
        val container = Wrapper(node, Sizing.content(), Sizing.content(), Algorithm.VERTICAL)
        container.alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP)

        // container.surface(Surface.outline(colors.random().argb()))
        container.padding(Insets.of(1))
        //container.debug()

        if (node.children.isNotEmpty()) {

            // Kinder horizontal anordnen
            val childContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content())
            childContainer.id("child-node")
            childContainer.alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP)
            childContainer.padding(Insets.of(2)) // Fügt Abstand zwischen den Kindknoten hinzu


            node.children.forEach {
                childContainer.child(getComponent(it))
            }

            container.child(childContainer)
        }

        return container
    }

    override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        super.draw(context, mouseX, mouseY, partialTicks, delta)

        RenderSystem.enableDepthTest()

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        val matrices = context.matrices
        matrices.push()
        matrices.translate(0f, 0f, 5.0f)
        context.drawTexture(
            shadow,
            scroll.x(),
            scroll.y(),
            0f,
            0f,
            scroll.width(),
            scroll.height(),
            scroll.width(),
            scroll.height(),
        )
        RenderSystem.disableBlend()
        matrices.pop()
    }

    inner class Wrapper(
        val node: TreeNode<ISkill>,
        horizontalSizing: Sizing = Sizing.content(),
        verticalSizing: Sizing = Sizing.content(),
        algorithm: Algorithm
    ) : FlowLayout(horizontalSizing, verticalSizing, algorithm) {
        val label = ScalableLabelComponent(node.value.title()).apply {
            shadow(true)
        }
        var isHoveredChild = false
        var isVisible = false
        val box: FlowLayout = Containers.verticalFlow(Sizing.fixed(26), Sizing.fixed(26)).apply {
            if (node.value.isUnlocked(MinecraftClient.getInstance().player!!)) {
            } else {
            }
            //child(label)
            cursorStyle(CursorStyle.POINTER)
            mouseEnter().subscribe {
                isHovered = true
                isHoveredChild = true
            }
            mouseLeave().subscribe {
                isHovered = false
                isHoveredChild = false
            }
            mouseDown().subscribe { _, _, _ ->
                if (isVisible) {
                    MinecraftClient.getInstance().soundManager.play(
                        PositionedSoundInstance.master(
                            SoundEvents.BLOCK_AMETHYST_BLOCK_HIT,
                            1.0f, 1f
                        )
                    )
                    node.value.skill()
                }
                return@subscribe true
            }
            child(node.value.icon())
            alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
        }


        init {
            margins(Insets.top(5))
            child(label)
            child(box)
        }

        fun anchorPoint(): Component {
            return box
        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {

            if (isVisible) {
                box.tooltip(node.value.tooltip(player))
            } else {
                box.tooltip(Text.empty())
            }

            val player = MinecraftClient.getInstance().player!!
            var drawLines = true
            if (node.value.isParentUnlocked(player)) {
                if (node.value.isUnlocked(player)) {
                    if (node.value.parent() == null) {
                        box.surface { context2, component ->
                            val root = "textures/gui/root_panel.png".toId()
                            context2.drawTexture(
                                root,
                                component.x(),
                                component.y(),
                                0f,
                                0f,
                                26,
                                26,
                                26,
                                26
                            )
                        }
                    } else {
                        box.surface { owoUIDrawContext, parentComponent ->
                            if (node.value.isLast()) {
                                val root =
                                    Identifier.ofVanilla("textures/gui/sprites/advancements/goal_frame_obtained.png")
                                owoUIDrawContext.drawTexture(
                                    root,
                                    parentComponent.x(),
                                    parentComponent.y(),
                                    0f,
                                    0f,
                                    26,
                                    26,
                                    26,
                                    26
                                )
                            } else {
                                NinePatchTexture.draw("unlocked".toId(), context, parentComponent)
                            }
                        }
                    }
                } else {
                    if (node.value.progress(player) == 1.0) {
                        box.surface { context2, component ->
                            val root = Identifier.ofVanilla("textures/gui/sprites/advancements/goal_frame_obtained.png")
                            context2.drawTexture(
                                root,
                                component.x(),
                                component.y(),
                                0f,
                                0f,
                                26,
                                26,
                                26,
                                26
                            )
                        }
                    } else {
                        if (node.value.isLast()) {
                            box.surface { owoUIDrawContext, parentComponent ->
                                val root =
                                    Identifier.ofVanilla("textures/gui/sprites/advancements/goal_frame_unobtained.png")
                                owoUIDrawContext.drawTexture(
                                    root,
                                    parentComponent.x(),
                                    parentComponent.y(),
                                    0f,
                                    0f,
                                    26,
                                    26,
                                    26,
                                    26
                                )
                            }
                        } else {
                            box.surface(Surface.PANEL)
                        }
                    }
                }
            } else {
                if (!node.value.isUnlocked(player) && node.value.parent()?.isParentUnlocked(player) == true) {
                    if (node.value.isLast()) {
                        box.surface { owoUIDrawContext, parentComponent ->
                            val root = "textures/gui/goal_frame_dark.png".toId()
                            owoUIDrawContext.drawTexture(
                                root,
                                parentComponent.x(),
                                parentComponent.y(),
                                0f,
                                0f,
                                26,
                                26,
                                26,
                                26
                            )
                        }
                    } else {
                        box.surface(Surface.DARK_PANEL)
                    }
                    drawLines = false
                } else {
                    isVisible = false
                    return
                }
            }

            isVisible = true


            if (!node.value.isUnlocked(MinecraftClient.getInstance().player!!)) {
                //  return
            }
            val childContainer = childById(FlowLayout::class.java, "child-node") as? FlowLayout? ?: return super.draw(
                context,
                mouseX,
                mouseY,
                partialTicks,
                delta
            )
            if (childContainer.children().isEmpty()) return super.draw(context, mouseX, mouseY, partialTicks, delta)

            val parentX = anchorPoint().x() + anchorPoint().width() / 2
            val parentY = anchorPoint().y() + anchorPoint().height() - 5

            // Berechne die mittlere Y-Position für die horizontale Linie
            val lowestChildY =
                childContainer.children().minOfOrNull { (it as? Wrapper)?.anchorPoint()?.y() ?: Int.MAX_VALUE }
                    ?: return super.draw(context, mouseX, mouseX, partialTicks, delta)
            val midY = parentY + (lowestChildY - parentY) / 2
            val whiteLineThickness = 2.0
            val grayedColor = Color.ofRgb(Colors.GRAY)
            val progressColor = Color.GREEN// Farbe für die Fortschrittslinie

            // Zeichne die graue Linie und die grüne Linie
            for ((index, child) in childContainer.children().withIndex()) {
                if (child is Wrapper) {
                    val childX = child.anchorPoint().x() + (child.anchorPoint().width()) / 2
                    val childY = child.anchorPoint().y()

                    // Zeichne die graue Linie
                    val angle =
                        Math.toDegrees(Math.atan2((childY - parentY).toDouble(), (childX - parentX).toDouble()));
                    val length =
                        Math.sqrt(((childX - parentX) * (childX - parentX) + (childY - parentY) * (childY - parentY)).toDouble());
                    if (drawLines) {
                        context.drawLine(parentX, parentY, angle, length, whiteLineThickness * 2, Color.BLACK);
                        context.drawLine(parentX, parentY, angle, length, whiteLineThickness, grayedColor);
                    }

                    val progress = child.node.value.progress(player)

                    // Berechne den Fortschritt-Endpunkt
                    val progressEndX = parentX + (childX - parentX) * progress
                    val progressEndY =
                        parentY + (childY - parentY) * progress / ((childY - parentY).toDouble().takeIf { it != 0.0 }
                            ?: 1.0)

                    // Zeichne die grüne Linie über der grauen Linie
                    val progressAngle = angle; // Verwende denselben Winkel
                    val progressLength = length * progress; // Berechne die Länge basierend auf dem Fortschritt

                    if (drawLines) {
                        context.drawLine(
                            parentX,
                            parentY,
                            progressAngle,
                            progressLength,
                            whiteLineThickness,
                            progressColor
                        );
                    }
                }
            }

            super.draw(context, mouseX, mouseY, partialTicks, delta)
        }


    }

    fun OwoUIDrawContext.drawLine(x1: Int, y1: Int, angle: Double, length: Double, thickness: Double, color: Color) {
        // Berechne die Endpunkte der Linie basierend auf dem Winkel
        val radians = Math.toRadians(angle)
        val x2 = (x1 + cos(radians) * length).toInt()
        val y2 = (y1 + sin(radians) * length).toInt()

        val offset =
            (Vector2d((x2 - x1).toDouble(), (y2 - y1).toDouble())).perpendicular().normalize().mul(thickness * 0.5f)
        val buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR)
        val matrix: Matrix4f = this.getMatrices().peek().getPositionMatrix()
        val vColor = color.argb()

        buffer.vertex(matrix, (x1.toDouble() + offset.x).toFloat(), (y1.toDouble() + offset.y).toFloat(), 0.0f)
            .color(vColor)
        buffer.vertex(matrix, (x1.toDouble() - offset.x).toFloat(), (y1.toDouble() - offset.y).toFloat(), 0.0f)
            .color(vColor)
        buffer.vertex(matrix, (x2.toDouble() - offset.x).toFloat(), (y2.toDouble() - offset.y).toFloat(), 0.0f)
            .color(vColor)
        buffer.vertex(matrix, (x2.toDouble() + offset.x).toFloat(), (y2.toDouble() + offset.y).toFloat(), 0.0f)
            .color(vColor)

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShader { GameRenderer.getPositionColorProgram() }
        BufferRenderer.drawWithGlobalProgram(buffer.end())
    }

}
