package org.maale.bible
import android.content.Context
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class BundledFts5P2InstrumentedTest {
 @Test fun fts5CreateInsertQueryRankRebuildAndUpgrade() {
  val context=ApplicationProvider.getApplicationContext<Context>()
  val file=context.getDatabasePath("p2-fts5.db"); file.parentFile?.mkdirs(); file.delete()
  val db=BundledSQLiteDriver().open(file.absolutePath)
  try {
   db.execSQL("CREATE TABLE docs(id INTEGER PRIMARY KEY, body TEXT NOT NULL)")
   db.execSQL("CREATE VIRTUAL TABLE docs_fts USING fts5(body, content='docs', content_rowid='id')")
   db.execSQL("INSERT INTO docs(id,body) VALUES(1,'synthetic alpha beta')")
   db.execSQL("INSERT INTO docs_fts(rowid,body) SELECT id,body FROM docs")
   db.prepare("SELECT rowid FROM docs_fts WHERE docs_fts MATCH 'alpha beta' ORDER BY rank").use { assertTrue(it.step()); assertEquals(1L,it.getLong(0)) }
   db.execSQL("DELETE FROM docs_fts"); db.execSQL("INSERT INTO docs_fts(docs_fts) VALUES('rebuild')")
   db.prepare("SELECT count(*) FROM docs_fts WHERE docs_fts MATCH 'alpha'").use { assertTrue(it.step()); assertEquals(1L,it.getLong(0)) }
   db.execSQL("ALTER TABLE docs ADD COLUMN schema_version INTEGER NOT NULL DEFAULT 1")
   db.prepare("SELECT schema_version FROM docs WHERE id=1").use { assertTrue(it.step()); assertEquals(1L,it.getLong(0)) }
  } finally { db.close(); file.delete() }
 }
}