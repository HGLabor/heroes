package gg.norisk.heroes.common.localization

import java.util.Locale

object LocalizationRegistry {
    val locales = hashMapOf<Locale, TranslationRegistry>()

    fun register(locale: Locale): TranslationRegistry {
        val translationRegistry = TranslationRegistry()
        locales[locale] = translationRegistry
        return translationRegistry
    }

    fun register(language: String): TranslationRegistry {
        val locale = Locale.of(language)
        return register(locale)
    }

    fun get(locale: Locale): TranslationRegistry? {
        return locales[locale] ?: locales[LocalizationManager.defaultLocale]
    }
}
