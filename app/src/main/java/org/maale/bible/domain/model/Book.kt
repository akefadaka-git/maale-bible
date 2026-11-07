package org.maale.bible.domain.model
data class Book(val order: Int, val usfmId: String, val englishName: String) {
    init {
        require(order > 0)
        require(usfmId.matches(Regex("[A-Z0-9]{3}")))
        require(englishName.isNotBlank())
    }
}
