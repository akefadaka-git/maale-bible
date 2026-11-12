package org.maale.bible.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject
import org.maale.bible.domain.model.*
import org.maale.bible.domain.repository.StudyRepository

/** Device-local user study metadata only. No scripture content is stored by this database. */
class StudyDatabase(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION), StudyRepository {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE reading_position (singleton INTEGER PRIMARY KEY CHECK(singleton=1), book_id TEXT NOT NULL, chapter INTEGER NOT NULL CHECK(chapter>0), verse INTEGER NOT NULL CHECK(verse>0), updated_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE bookmarks (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL CHECK(chapter>0), verse INTEGER NOT NULL CHECK(verse>0), created_at INTEGER NOT NULL, label TEXT NOT NULL DEFAULT '')")
        db.execSQL("CREATE INDEX idx_bookmarks_ref ON bookmarks(book_id,chapter,verse)")
        db.execSQL("CREATE TABLE annotations (id TEXT PRIMARY KEY, book_id TEXT NOT NULL, chapter INTEGER NOT NULL CHECK(chapter>0), verse INTEGER NOT NULL CHECK(verse>0), kind TEXT NOT NULL CHECK(kind IN ('HIGHLIGHT','NOTE')), body TEXT NOT NULL, color TEXT, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX idx_annotations_ref ON annotations(book_id,chapter,verse)")
        db.execSQL("CREATE TABLE reading_plans (id TEXT PRIMARY KEY, title TEXT NOT NULL, start_day INTEGER NOT NULL, duration_days INTEGER NOT NULL, completed_days TEXT NOT NULL, updated_at INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX idx_plans_updated ON reading_plans(updated_at)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2 && newVersion >= 2) {
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_plans_updated ON reading_plans(updated_at)")
        }
        // Future upgrades must add ordered, non-destructive migrations. Never drop user data.
    }

    override fun saveReadingPosition(position: ReadingPosition) {
        writableDatabase.insertWithOnConflict("reading_position",null,ContentValues().apply {
            put("singleton",1); put("book_id",position.bookId); put("chapter",position.chapter); put("verse",position.verse); put("updated_at",position.updatedAtEpochMs)
        },SQLiteDatabase.CONFLICT_REPLACE)
    }
    override fun getReadingPosition(): ReadingPosition? = readableDatabase.rawQuery("SELECT book_id,chapter,verse,updated_at FROM reading_position WHERE singleton=1",null).use {
        if (!it.moveToFirst()) null else ReadingPosition(it.getString(0),it.getInt(1),it.getInt(2),it.getLong(3))
    }

    override fun addBookmark(bookmark: Bookmark) { writableDatabase.insertOrThrow("bookmarks",null,bookmarkValues(bookmark)) }
    override fun removeBookmark(id: String) { writableDatabase.delete("bookmarks","id=?",arrayOf(id)) }
    override fun getBookmarks(bookId: String?): List<Bookmark> {
        val rows=mutableListOf<Bookmark>()
        val sql=if(bookId==null) "SELECT id,book_id,chapter,verse,created_at,label FROM bookmarks ORDER BY created_at DESC" else "SELECT id,book_id,chapter,verse,created_at,label FROM bookmarks WHERE book_id=? ORDER BY created_at DESC"
        readableDatabase.rawQuery(sql,if(bookId==null)null else arrayOf(bookId)).use { c ->
            while(c.moveToNext()) rows += Bookmark(c.getString(0),c.getString(1),c.getInt(2),c.getInt(3),c.getLong(4),c.getString(5))
        }; return rows
    }
    private fun bookmarkValues(b: Bookmark)=ContentValues().apply { put("id",b.id);put("book_id",b.bookId);put("chapter",b.chapter);put("verse",b.verse);put("created_at",b.createdAtEpochMs);put("label",b.label) }

    override fun saveAnnotation(annotation: StudyAnnotation) { writableDatabase.insertWithOnConflict("annotations",null,annotationValues(annotation),SQLiteDatabase.CONFLICT_REPLACE) }
    override fun deleteAnnotation(id: String) { writableDatabase.delete("annotations","id=?",arrayOf(id)) }
    override fun getAnnotations(bookId: String?): List<StudyAnnotation> {
        val rows=mutableListOf<StudyAnnotation>()
        val sql=if(bookId==null) "SELECT id,book_id,chapter,verse,kind,body,color,created_at,updated_at FROM annotations ORDER BY updated_at DESC" else "SELECT id,book_id,chapter,verse,kind,body,color,created_at,updated_at FROM annotations WHERE book_id=? ORDER BY updated_at DESC"
        readableDatabase.rawQuery(sql,if(bookId==null)null else arrayOf(bookId)).use { c ->
            while(c.moveToNext()) rows += StudyAnnotation(c.getString(0),c.getString(1),c.getInt(2),c.getInt(3),AnnotationKind.valueOf(c.getString(4)),c.getString(5),if(c.isNull(6))null else c.getString(6),c.getLong(7),c.getLong(8))
        }; return rows
    }
    private fun annotationValues(a: StudyAnnotation)=ContentValues().apply { put("id",a.id);put("book_id",a.bookId);put("chapter",a.chapter);put("verse",a.verse);put("kind",a.kind.name);put("body",a.body);put("color",a.color);put("created_at",a.createdAtEpochMs);put("updated_at",a.updatedAtEpochMs) }

    override fun savePlan(plan: ReadingPlan) { writableDatabase.insertWithOnConflict("reading_plans",null,planValues(plan),SQLiteDatabase.CONFLICT_REPLACE) }
    override fun getPlans(): List<ReadingPlan> {
        val rows=mutableListOf<ReadingPlan>()
        readableDatabase.rawQuery("SELECT id,title,start_day,duration_days,completed_days,updated_at FROM reading_plans ORDER BY updated_at DESC",null).use { c ->
            while(c.moveToNext()) rows += ReadingPlan(c.getString(0),c.getString(1),c.getLong(2),c.getInt(3),c.getString(4).split(",").filter(String::isNotBlank).map(String::toInt).toSet(),c.getLong(5))
        }; return rows
    }
    private fun planValues(p: ReadingPlan)=ContentValues().apply { put("id",p.id);put("title",p.title);put("start_day",p.startDateEpochDay);put("duration_days",p.durationDays);put("completed_days",p.completedDays.sorted().joinToString(","));put("updated_at",p.updatedAtEpochMs) }

    override fun exportBackup(): String {
        val root=JSONObject().put("format","maale-bible-study-backup").put("version",1)
        getReadingPosition()?.let { root.put("readingPosition",JSONObject().put("bookId",it.bookId).put("chapter",it.chapter).put("verse",it.verse).put("updatedAt",it.updatedAtEpochMs)) }
        root.put("bookmarks",JSONArray().also { a -> getBookmarks().forEach { a.put(JSONObject().put("id",it.id).put("bookId",it.bookId).put("chapter",it.chapter).put("verse",it.verse).put("createdAt",it.createdAtEpochMs).put("label",it.label)) } })
        root.put("annotations",JSONArray().also { a -> getAnnotations().forEach { a.put(JSONObject().put("id",it.id).put("bookId",it.bookId).put("chapter",it.chapter).put("verse",it.verse).put("kind",it.kind.name).put("body",it.body).put("color",it.color).put("createdAt",it.createdAtEpochMs).put("updatedAt",it.updatedAtEpochMs)) } })
        root.put("plans",JSONArray().also { a -> getPlans().forEach { p -> a.put(JSONObject().put("id",p.id).put("title",p.title).put("startDay",p.startDateEpochDay).put("durationDays",p.durationDays).put("completedDays",JSONArray(p.completedDays.sorted())).put("updatedAt",p.updatedAtEpochMs)) } })
        return root.toString()
    }

    override fun restoreBackup(json: String) {
        val root=JSONObject(json)
        require(root.optString("format")=="maale-bible-study-backup" && root.optInt("version")==1) { "Unsupported backup format" }
        // Validate every record before opening the transaction; malformed backups cannot erase data.
        val position=root.optJSONObject("readingPosition")?.let { ReadingPosition(it.getString("bookId"),it.getInt("chapter"),it.getInt("verse"),it.getLong("updatedAt")) }
        val ba=root.optJSONArray("bookmarks")?:JSONArray()
        val aa=root.optJSONArray("annotations")?:JSONArray()
        val pa=root.optJSONArray("plans")?:JSONArray()
        val bookmarks=(0 until ba.length()).map { i -> ba.getJSONObject(i).let { Bookmark(it.getString("id"),it.getString("bookId"),it.getInt("chapter"),it.getInt("verse"),it.getLong("createdAt"),it.optString("label")) } }
        val annotations=(0 until aa.length()).map { i -> aa.getJSONObject(i).let { StudyAnnotation(it.getString("id"),it.getString("bookId"),it.getInt("chapter"),it.getInt("verse"),AnnotationKind.valueOf(it.getString("kind")),it.optString("body"),it.optString("color").takeIf { v -> v.isNotEmpty() },it.getLong("createdAt"),it.getLong("updatedAt")) } }
        val plans=(0 until pa.length()).map { i -> pa.getJSONObject(i).let { o -> val days=o.getJSONArray("completedDays"); ReadingPlan(o.getString("id"),o.getString("title"),o.getLong("startDay"),o.getInt("durationDays"),(0 until days.length()).map { days.getInt(it) }.toSet(),o.getLong("updatedAt")) } }
        val db=writableDatabase; db.beginTransaction()
        try {
            db.delete("reading_position",null,null);db.delete("bookmarks",null,null);db.delete("annotations",null,null);db.delete("reading_plans",null,null)
            position?.let { p -> db.insertOrThrow("reading_position",null,ContentValues().apply { put("singleton",1);put("book_id",p.bookId);put("chapter",p.chapter);put("verse",p.verse);put("updated_at",p.updatedAtEpochMs) }) }
            bookmarks.forEach { db.insertOrThrow("bookmarks",null,bookmarkValues(it)) }
            annotations.forEach { db.insertOrThrow("annotations",null,annotationValues(it)) }
            plans.forEach { db.insertOrThrow("reading_plans",null,planValues(it)) }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }
    companion object { const val DB_NAME="maale_bible_study.db"; const val DB_VERSION=2 }
}
