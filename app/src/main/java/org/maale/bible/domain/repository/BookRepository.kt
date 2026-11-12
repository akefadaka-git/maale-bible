package org.maale.bible.domain.repository
import org.maale.bible.domain.model.Book
interface BookRepository {
    fun getAllBooks(): List<Book>
    fun getBook(usfmId: String): Book?
}
