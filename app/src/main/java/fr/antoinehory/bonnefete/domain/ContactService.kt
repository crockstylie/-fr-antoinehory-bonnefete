package fr.antoinehory.bonnefete.domain

import android.content.Context
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.Normalizer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to interact with system contacts.
 */
@Singleton
class ContactService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Cleans the saint name by removing "Saint " prefix and normalizing (removing accents).
     */
    private fun normalizeName(name: String): String {
        return name
            .replace("Saint ", "", ignoreCase = true)
            .replace("Sainte ", "", ignoreCase = true)
            .normalize()
    }

    private fun String.normalize(): String {
        return Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
            .trim()
    }

    /**
     * Returns a list of contact names matching the given name.
     */
    fun getMatchingContacts(saintName: String): List<String> {
        if (saintName.isBlank()) return emptyList()

        val normalizedSaint = normalizeName(saintName)
        val matches = mutableListOf<String>()
        
        val uri = ContactsContract.Contacts.CONTENT_URI
        val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            while (it.moveToNext()) {
                if (nameIndex >= 0) {
                    val contactName = it.getString(nameIndex)
                    if (contactName.normalize().contains(normalizedSaint)) {
                        matches.add(contactName)
                    }
                }
            }
        }
        return matches.distinct()
    }
}
