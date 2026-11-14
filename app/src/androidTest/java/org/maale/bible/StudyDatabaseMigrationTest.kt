package org.maale.bible

import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.maale.bible.data.repository.StudyDatabase

@RunWith(AndroidJUnit4::class)
class StudyDatabaseMigrationTest {
    @Test fun versionOneDatabaseUpgradesWithoutDroppingPlanRows() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(StudyDatabase.DB_NAME)
        val file=context.getDatabasePath(StudyDatabase.DB_NAME)
        file.parentFile?.mkdirs()
        val old=SQLiteDatabase.openOrCreateDatabase(file,null)
        old.execSQL("CREATE TABLE reading_plans (id TEXT PRIMARY KEY, title TEXT NOT NULL, start_day INTEGER NOT NULL, duration_days INTEGER NOT NULL, completed_days TEXT NOT NULL, updated_at INTEGER NOT NULL)")
        old.execSQL("INSERT INTO reading_plans VALUES ('legacy','Legacy plan',1,7,'1,2',10)")
        old.version=1
        old.close()

        val upgraded=StudyDatabase(context)
        val db=upgraded.writableDatabase
        db.rawQuery("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_plans_updated'",null).use { assertTrue(it.moveToFirst()) }
        db.rawQuery("SELECT id FROM reading_plans WHERE id='legacy'",null).use { assertTrue(it.moveToFirst()) }
        upgraded.close()
        context.deleteDatabase(StudyDatabase.DB_NAME)
    }
}
