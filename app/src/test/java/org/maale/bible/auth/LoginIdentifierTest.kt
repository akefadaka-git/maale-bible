package org.maale.bible.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maale.bible.domain.auth.LoginIdentifier
import org.maale.bible.domain.auth.RegistrationIdentity

class LoginIdentifierTest {
    @Test
    fun usernameIsTrimmedCaseFoldedAndUnicodeNormalized() {
        val identifier = LoginIdentifier.parse("  Ａkefa_01  ")
        assertEquals("akefa_01", identifier.canonicalValue)
        assertTrue(identifier is LoginIdentifier.Username)
    }

    @Test
    fun mobileNumberAcceptsInternationalFormattingWithoutInferringCountry() {
        val identifier = LoginIdentifier.parse("+251 91-234-5678")
        assertEquals("+251912345678", identifier.canonicalValue)
        assertTrue(identifier is LoginIdentifier.MobileNumber)
    }

    @Test
    fun registrationRequiresBothNormalizedIdentifiers() {
        val identity = RegistrationIdentity.parse("Akefa_01", "+251 91 234 5678")
        assertEquals("akefa_01", identity.username.canonicalValue)
        assertEquals("+251912345678", identity.mobileNumber.canonicalValue)
    }

    @Test
    fun rejectsUsernameOutsideDocumentedSyntax() {
        assertThrows(IllegalArgumentException::class.java) {
            LoginIdentifier.parse("ab")
        }
        assertThrows(IllegalArgumentException::class.java) {
            LoginIdentifier.parse("akefa name")
        }
    }

    @Test
    fun rejectsPhoneWithoutExplicitInternationalCountryCode() {
        assertThrows(IllegalArgumentException::class.java) {
            LoginIdentifier.parse("0912345678")
        }
        assertThrows(IllegalArgumentException::class.java) {
            LoginIdentifier.parse("+0123456789")
        }
    }

    @Test
    fun rejectsEmptyLoginIdentifier() {
        assertThrows(IllegalArgumentException::class.java) {
            LoginIdentifier.parse("   ")
        }
    }
}
