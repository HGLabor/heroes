package gg.norisk.heroes.common.localization

import gg.norisk.heroes.common.localization.minimessage.minimessage
import kotlinx.serialization.json.Json
import net.minecraft.text.Text
import java.io.File

class TranslationRegistry {
    val translations: HashMap<String, String> = hashMapOf()

    fun register(key: String, value: String) {
        translations[key] = value
    }

    fun register(translation: Pair<String, String>) {
        val (key, value) = translation
        return register(key, value)
    }

    fun registerAllFromFile(file: File) {
        val fileContent = file.readText()
        val fileTranslations = runCatching {
            Json.decodeFromString<HashMap<String, String>>(fileContent)
        }.getOrNull() ?: hashMapOf()

        fileTranslations.forEach { (key, value) ->
            register(key, value)
        }
    }

    fun get(key: String): String {
        return translations[key] ?: key
    }

    fun getFormatted(key: String, vararg args: Any): String {
        val string = get(key)
        val formattedString = string.format(*args)
        return formattedString
    }

    fun getAsText(key: String, vararg args: Any): Text {
        val formattedString = getFormatted(key, *args)
        return Text.literal(formattedString)
    }

    fun getAsMinimessage(key: String, vararg args: Any): Text {
        val formattedString = getFormatted(key, *args)
        return minimessage(formattedString)
    }
}
