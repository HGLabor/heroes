package gg.norisk.heroes.common.localization

import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.common.localization.minimessage.MiniMessageUtils
import gg.norisk.heroes.common.utils.createIfNotExists
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.resource.language.I18n
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.silkmc.silk.core.text.literalText
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

object LocalizationManager {
    private val langDir by lazy {
        val configDir = if (FabricLoader.getInstance().environmentType == EnvType.SERVER) {
            Path(FabricLoader.getInstance().configDir.absolutePathString(), "heroes").toFile().createIfNotExists()
        } else {
            null
        }
        configDir
    }
    val defaultLocale = Locale.ENGLISH

    fun init() {
        loadAllLanguageFiles()
    }

    private fun loadAllLanguageFiles() {
        langDir?.listFiles()?.forEach { langFile ->
            val locale = runCatching { Locale.of(langFile.nameWithoutExtension) }
                .onFailure {
                    print("Localization error: ")
                    it.printStackTrace()
                }.getOrNull() ?: defaultLocale

            LocalizationRegistry.register(locale).registerAllFromFile(langFile)
        }
    }

    @Environment(EnvType.SERVER)
    fun getLocalizedTextForPlayerServer(player: ServerPlayerEntity, key: String, vararg args: Any): Text {
        val language = player.clientOptions.language
        val locale = Locale.of(language)
        val translations = LocalizationRegistry.get(locale)
        if (translations == null) {
            HeroesManager.logger.warn("translationRegistry for locale `${locale}` does not exist. all locales: ${LocalizationRegistry.locales.keys}")
        }

        val minimessage = translations?.getAsMinimessage(key, *args)
        if (minimessage == null) {
            HeroesManager.logger.warn("Translation for key `$key` does not exist.")
        }
        val text = minimessage
            ?: literalText {
                text(key)
                if (args.isNotEmpty()) {
                    text(" ${args.joinToString()}")
                }
            }
        return text
    }

    @Environment(EnvType.CLIENT)
    fun getLocalizedTextOnClient(key: String, vararg args: Any): Text {
        val translatedText = I18n.translate(key, *args)
        val formatted = MiniMessageUtils.deserialize(translatedText)
        return formatted
    }
}

@Environment(EnvType.CLIENT)
fun localizedText(key: String, vararg args: Any): Text {
    return LocalizationManager.getLocalizedTextOnClient(key, *args)
}

fun PlayerEntity.getLocalized(key: String, vararg args: Any): Text {
    return if (this is ClientPlayerEntity) {
        println("is client player")
        LocalizationManager.getLocalizedTextOnClient(key, *args)
    } else {
        println("is server player")
        LocalizationManager.getLocalizedTextForPlayerServer(this as ServerPlayerEntity, key, *args)
    }
}

fun PlayerEntity.sendLocalized(key: String, vararg args: Any) {
    sendMessage(this.getLocalized(key, *args), false)
}
