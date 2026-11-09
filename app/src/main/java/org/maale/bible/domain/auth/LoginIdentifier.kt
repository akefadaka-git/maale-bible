package org.maale.bible.domain.auth

import java.text.Normalizer
import java.util.Locale

/**
 * A login identifier accepted by the authentication API.
 *
 * Values are normalized before they cross the repository boundary. This type
 * performs syntax normalization only; uniqueness and ownership are enforced
 * authoritatively by the backend.
 */
sealed interface LoginIdentifier {
    val canonicalValue: String

    data class Username private constructor(
        override val canonicalValue: String,
    ) : LoginIdentifier {
        companion object {
            private val pattern = Regex("[a-z][a-z0-9._-]{2,29}")

            fun parse(raw: String): Username {
                val normalized = Normalizer.normalize(raw.trim(), Normalizer.Form.NFKC)
                    .lowercase(Locale.ROOT)
                require(pattern.matches(normalized)) {
                    "Username must be 3–30 characters, start with a letter, and contain only letters, numbers, dot, underscore or hyphen."
                }
                return Username(normalized)
            }
        }
    }

    data class MobileNumber private constructor(
        override val canonicalValue: String,
    ) : LoginIdentifier {
        companion object {
            private val formatting = Regex("[\\s().-]")
            private val e164 = Regex("\\+[1-9][0-9]{7,14}")

            /**
             * Accepts an international number and removes common visual separators.
             * A country/region is never inferred; callers must provide the country code.
             */
            fun parse(raw: String): MobileNumber {
                val normalized = raw.trim().replace(formatting, "")
                require(e164.matches(normalized)) {
                    "Mobile number must include an international country code in E.164 form."
                }
                return MobileNumber(normalized)
            }
        }
    }

    companion object {
        /** '+' selects the phone namespace; all other input is parsed as a username. */
        fun parse(raw: String): LoginIdentifier {
            val candidate = raw.trim()
            require(candidate.isNotEmpty()) { "Enter a mobile number or username." }
            return if (candidate.startsWith("+")) {
                MobileNumber.parse(candidate)
            } else {
                Username.parse(candidate)
            }
        }
    }
}

/**
 * The two unique identifiers collected during account registration.
 * This is a client-side shape only; the backend must enforce uniqueness.
 */
data class RegistrationIdentity(
    val username: LoginIdentifier.Username,
    val mobileNumber: LoginIdentifier.MobileNumber,
) {
    companion object {
        fun parse(username: String, mobileNumber: String) = RegistrationIdentity(
            username = LoginIdentifier.Username.parse(username),
            mobileNumber = LoginIdentifier.MobileNumber.parse(mobileNumber),
        )
    }
}
