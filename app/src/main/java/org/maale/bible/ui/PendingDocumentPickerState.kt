package org.maale.bible.ui

import androidx.lifecycle.ViewModel

/**
 * Holds only transient secrets needed while an Android document picker is in flight.
 *
 * ViewModel memory survives Activity configuration recreation, but values are not
 * serialized into saved-instance state and are cleared when the Activity is finished.
 */
class PendingDocumentPickerState : ViewModel() {
    private var pendingExportKey: String? = null
    private var pendingRestoreKey: String? = null

    fun storeExportKey(key: String) {
        require(key.isNotBlank())
        pendingExportKey = key
    }

    fun consumeExportKey(): String? = pendingExportKey.also { pendingExportKey = null }

    fun clearExportKey() {
        pendingExportKey = null
    }

    fun storeRestoreKey(key: String) {
        require(key.isNotBlank())
        pendingRestoreKey = key
    }

    fun consumeRestoreKey(): String? = pendingRestoreKey.also { pendingRestoreKey = null }

    fun clearRestoreKey() {
        pendingRestoreKey = null
    }

    override fun onCleared() {
        clearExportKey()
        clearRestoreKey()
    }
}
