package gg.norisk.heroes.client.option

import gg.norisk.heroes.common.hero.getHero
import gg.norisk.utils.events.KeyEvents
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object HeroKeyBindings {
    val firstKeyBind = if (FabricLoader.getInstance().environmentType == EnvType.SERVER) null
    else KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.heroes.first",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "category.heroes.abilities"
        )
    )

    val secondKeyBind = if (FabricLoader.getInstance().environmentType == EnvType.SERVER) null
    else KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.heroes.second", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_V, // The keycode of the key
            "category.heroes.abilities" // The translation key of the keybinding's category.
        )
    )

    val thirdKeyBind = if (FabricLoader.getInstance().environmentType == EnvType.SERVER) null
    else KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.heroes.third", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_G, // The keycode of the key
            "category.heroes.abilities" // The translation key of the keybinding's category.
        )
    )

    val fourthKeyBinding = if (FabricLoader.getInstance().environmentType == EnvType.SERVER) null
    else KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.heroes.fourth", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_H, // The keycode of the key
            "category.heroes.abilities" // The translation key of the keybinding's category.
        )
    )

    val fifthKeyBind = if (FabricLoader.getInstance().environmentType == EnvType.SERVER) null
    else KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.heroes.fifth", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_Z, // The keycode of the key
            "category.heroes.abilities" // The translation key of the keybinding's category.
        )
    )

    val pickItemKeyBinding by lazy {
        if (FabricLoader.getInstance().environmentType == EnvType.SERVER) null
        else MinecraftClient.getInstance().options.pickItemKey
    }

    val heroKeyBindings by lazy { listOf(firstKeyBind, secondKeyBind, thirdKeyBind, fourthKeyBinding, fifthKeyBind) }
    //TODO das maybe als config damit wir lvie updaten können?
    val blacklist = mutableSetOf("key.voice_chat", "key.voice_chat_group", "key.hide_icons")

    @Environment(EnvType.CLIENT)
    fun initClient() {
        KeyEvents.keyBindingOnPressEvent.listen { event ->
            val player = MinecraftClient.getInstance().player ?: return@listen
            if (player.getHero() == null) return@listen
            if (blacklist.contains(event.keybinding.translationKey)) {
                if (heroKeyBindings.any { it?.equals(event.keybinding) == true}) {
                    event.isCancelled.set(true)
                }
            }
        }
    }
}
