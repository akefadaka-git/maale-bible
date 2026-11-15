package org.maale.bible.data.scripture

import android.content.Context

/**
 * Shared reader edition preference. Search, study and quiz surfaces should read this same
 * store rather than maintaining independent edition selections.
 */
object ScriptureEditionSelectionStore {
    private const val PREFS = "scripture_edition_preferences"
    private const val KEY_SELECTED_EDITION = "selected_edition_id"

    fun load(context: Context): String {
        val stored = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_EDITION, null)
        return stored?.takeIf { ScriptureEditionCatalog.find(it) != null }
            ?: ScriptureEditionCatalog.PRIMARY_MAALE_ID
    }

    fun save(context: Context, editionId: String): Boolean {
        if (ScriptureEditionCatalog.find(editionId) == null) return false
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELECTED_EDITION, editionId)
            .apply()
        return true
    }
}
