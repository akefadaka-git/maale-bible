package org.maale.bible.ui

import android.content.Context

enum class AppLanguage(val tag: String) {
    MAALE("mdy"),
    AMHARIC("am"),
    ENGLISH("en");

    companion object {
        private const val PREFS = "maale_bible_preferences"
        private const val KEY_LANGUAGE = "ui_language"

        fun load(context: Context): AppLanguage {
            val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_LANGUAGE, MAALE.tag)
            return entries.firstOrNull { it.tag == saved } ?: MAALE
        }

        fun save(context: Context, language: AppLanguage) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANGUAGE, language.tag)
                .apply()
        }
    }
}
