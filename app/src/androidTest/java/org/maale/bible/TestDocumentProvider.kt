package org.maale.bible

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File

/** Test-only document endpoint used to exercise the same ContentResolver I/O as SAF. */
class TestDocumentProvider : ContentProvider() {
    override fun onCreate(): Boolean = true
    override fun getType(uri: Uri): String = "application/json"
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = throw UnsupportedOperationException()
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        val name = requireNotNull(uri.lastPathSegment)
        require(name in setOf("backup.json", "invalid.json")) { "Unexpected test document" }
        val file = File(requireNotNull(context).cacheDir, "maale-picker-test-$name")
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.parseMode(mode))
    }
}
