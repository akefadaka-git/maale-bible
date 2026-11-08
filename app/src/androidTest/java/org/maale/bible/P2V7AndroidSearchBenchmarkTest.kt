package org.maale.bible

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class P2V7AndroidSearchBenchmarkTest {
    @Test fun bundledFts5WarmSearchOnOneHundredThousandSyntheticRows() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = context.getDatabasePath("p2-v7-benchmark.db")
        file.parentFile?.mkdirs()
        file.delete()
        val db = BundledSQLiteDriver().open(file.absolutePath)
        try {
            db.prepare("PRAGMA journal_mode = OFF").use { it.step() }
            db.prepare("PRAGMA synchronous = OFF").use { it.step() }
            db.prepare("CREATE TABLE docs(id INTEGER PRIMARY KEY, body TEXT NOT NULL)").use { it.step() }
            db.prepare("CREATE VIRTUAL TABLE docs_fts USING fts5(body, content='docs', content_rowid='id')").use { it.step() }
            db.prepare("""
                WITH RECURSIVE rows(x) AS (
                    SELECT 1 UNION ALL SELECT x + 1 FROM rows WHERE x < 100000
                )
                INSERT INTO docs(id, body)
                SELECT x,
                    CASE WHEN x % 1000 = 0
                        THEN 'synthetic alpha beta reference row ' || x
                        ELSE 'synthetic unrelated reference row ' || x
                    END
                FROM rows
            """.trimIndent()).use { it.step() }
            db.prepare("INSERT INTO docs_fts(rowid, body) SELECT id, body FROM docs").use { it.step() }

            val durations = LongArray(30)
            repeat(30) { index ->
                val started = System.nanoTime()
                db.prepare("SELECT rowid FROM docs_fts WHERE docs_fts MATCH 'alpha beta' ORDER BY rank LIMIT 20")
                    .use { statement ->
                        var count = 0
                        while (statement.step()) count++
                        assertEquals(20, count)
                    }
                durations[index] = System.nanoTime() - started
            }
            val sorted = durations.sorted()
            val p50 = TimeUnit.NANOSECONDS.toMillis(sorted[14])
            val p95 = TimeUnit.NANOSECONDS.toMillis(sorted[28])
            val report = "P2V7 api=${Build.VERSION.SDK_INT} model=${Build.MODEL} records=100000 " +
                "matching_records=100 queries=30 p50_ms=$p50 p95_ms=$p95"
            android.util.Log.i("P2V7_BENCHMARK", report)
            assertTrue("Warm selective FTS5 search p95 exceeded 300ms: $report", p95 <= 300)
        } finally {
            db.close()
            file.delete()
        }
    }
}
