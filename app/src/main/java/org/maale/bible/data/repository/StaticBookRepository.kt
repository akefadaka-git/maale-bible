package org.maale.bible.data.repository
import org.maale.bible.data.catalog.CanonicalBookCatalog
import org.maale.bible.domain.model.Book
import org.maale.bible.domain.repository.BookRepository
class StaticBookRepository : BookRepository {
    override fun getAllBooks(): List<Book> = CanonicalBookCatalog.books
    override fun getBook(usfmId: String): Book? = CanonicalBookCatalog.find(usfmId)
}
