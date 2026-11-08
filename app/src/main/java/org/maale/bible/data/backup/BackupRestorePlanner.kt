package org.maale.bible.data.backup

import java.security.MessageDigest

enum class BackupRestoreMode { MERGE, REPLACE }

data class BackupRestorePreview(
    val additions: Set<String>,
    val conflicts: Set<String>,
    val unchanged: Set<String>,
) {
    val requiresReplaceConfirmation: Boolean get() = false
}

/**
 * Computes a non-destructive restore result without writing to the database.
 * Persistence integration must apply the returned result inside one transaction.
 */
object BackupRestorePlanner {
    fun preview(
        existing: Map<String, ByteArray>,
        incoming: Map<String, ByteArray>,
    ): BackupRestorePreview {
        val additions = incoming.keys - existing.keys
        val conflicts = incoming.keys.intersect(existing.keys).filterTo(mutableSetOf()) { path ->
            !MessageDigest.isEqual(existing.getValue(path), incoming.getValue(path))
        }
        val unchanged = incoming.keys.intersect(existing.keys) - conflicts
        return BackupRestorePreview(additions, conflicts, unchanged)
    }

    fun plan(
        existing: Map<String, ByteArray>,
        incoming: Map<String, ByteArray>,
        mode: BackupRestoreMode = BackupRestoreMode.MERGE,
        confirmReplace: Boolean = false,
    ): Map<String, ByteArray> {
        if (mode == BackupRestoreMode.REPLACE) {
            require(confirmReplace) { "Replace restore requires explicit confirmation" }
            return incoming.mapValues { it.value.copyOf() }
        }
        // Merge never overwrites a conflicting local record; the preview exposes conflicts.
        val result = existing.mapValuesTo(linkedMapOf()) { it.value.copyOf() }
        incoming.forEach { (path, bytes) ->
            if (path !in result) result[path] = bytes.copyOf()
        }
        return result
    }
}
