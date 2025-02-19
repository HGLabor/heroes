package gg.norisk.heroes.common.localization.minimessage

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.ParsingException
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver
import net.kyori.adventure.text.minimessage.internal.serializer.StyleClaim
import net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

internal class LaborColorTagResolver : TagResolver, SerializableResolver.Single {
    @Throws(ParsingException::class)
    override fun resolve(name: String, args: ArgumentQueue, ctx: Context): Tag? {
        if (!this.has(name)) {
            return null
        }
        val colorName = if (isLaborColorOrAbbreviation(name)) {
            args.popOr("Expected to find a color parameter: <name>|#RRGGBB").lowerValue()
        } else {
            name
        }

        val color = resolveColor(colorName, ctx)
        return Tag.styling(color)
    }

    override fun has(name: String): Boolean {
        return isLaborColorOrAbbreviation(name) || COLOR_ALIASES.containsKey(name)
    }

    override fun claimStyle(): StyleClaim<*>? {
        return STYLE
    }

    companion object {
        private const val LABORCOLOR_3 = "lc"
        private const val LABORCOLOR_2 = "laborColour"
        private const val LABORCOLOR = "laborColor"
        val laborColors = hashMapOf<String, Int>()

        val INSTANCE: TagResolver = LaborColorTagResolver()
        private val STYLE = StyleClaim.claim(
            LABORCOLOR,
            { obj: Style -> obj.color() },
            { color: TextColor, emitter: TokenEmitter ->
                // TODO: custom aliases
                // TODO: compact vs expanded format? COLOR vs color:COLOR vs c:COLOR
                if (color is NamedTextColor) {
                    emitter.tag(NamedTextColor.NAMES.key(color)!!)
                } else {
                    emitter.tag(color.asHexString())
                }
            })

        private val COLOR_ALIASES: MutableMap<String, TextColor> = HashMap()

        init {
            LaborColors.getAllColorsWithValue().forEach { color, value ->
                laborColors[color] = value
            }
        }

        private fun isLaborColorOrAbbreviation(name: String): Boolean {
            return name == LABORCOLOR || name == LABORCOLOR_2 || name == LABORCOLOR_3
        }

        @Throws(ParsingException::class)
        fun resolveColor(colorName: String, ctx: Context): TextColor {
            val color = if (laborColors.containsKey(colorName.lowercase())) {
                TextColor.color(laborColors[colorName]!!)
            } else if (COLOR_ALIASES.containsKey(colorName)) {
                COLOR_ALIASES[colorName]
            } else if (colorName[0] == TextColor.HEX_CHARACTER) {
                TextColor.fromHexString(colorName)
            } else {
                NamedTextColor.NAMES.value(colorName)
            }

            if (color == null) {
                throw ctx.newException(
                    String.format(
                        "Unable to parse a color from '%s'. Please use named.",
                        colorName
                    )
                )
            }
            return color
        }
    }
}
