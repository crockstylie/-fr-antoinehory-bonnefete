package fr.antoinehory.bonnefete.domain

import android.content.Context
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.Normalizer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service chargé d'interagir avec le répertoire de contacts du téléphone
 * et de déterminer si un contact porte le prénom du saint du jour.
 */
@Singleton
class ContactService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val WORD_TOKEN_REGEX = Regex("[a-z0-9]+(?:-[a-z0-9]+)*")
        private val SAINT_PREFIX_REGEX = Regex("^(?:sainte?s?\\s+)+", RegexOption.IGNORE_CASE)
        private val EPITHET_SUFFIX_REGEX = Regex("\\s+(?:de\\s+la|de\\s+l'|du|des|de|d'|le|la)\\b.*$", RegexOption.IGNORE_CASE)

        /**
         * Fêtes calendaires qui ne correspondent pas à des prénoms de personnes.
         */
        private val NON_FIRST_NAME_CELEBRATIONS = setOf(
            "jour de l'an",
            "fete de la presentation",
            "notre dame de lourdes",
            "mercredi des cendres",
            "fete du travail",
            "armistice de 1945",
            "solstice d'ete",
            "fete nationale",
            "fete de notre dame du mont carmel",
            "fete de la transfiguration",
            "fete de l'assomption",
            "fete de la nativite",
            "fete de la croix glorieuse",
            "equinoxe d'automne",
            "toussaint",
            "fete des defunts",
            "armistice de 1918",
            "presence de marie",
            "fete de l'immaculee conception",
            "solstice d'hiver",
            "noel",
            "innocents",
            "conversion de paul"
        )
    }

    /**
     * Normalise une chaîne de caractères (suppression des accents NFD, ligatures, mise en minuscules).
     */
    internal fun normalizeText(input: String): String {
        val decomposed = Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .replace("œ", "oe", ignoreCase = true)
            .replace("æ", "ae", ignoreCase = true)
            .lowercase()
            .trim()
        return decomposed
    }

    /**
     * Nettoie le nom du saint en retirant les préfixes ("Saint ", "Sainte ") et les épithètes historiques
     * ("de Padoue", "d'Assise", "le Bon", etc.), puis normalise le prénom obtenu.
     */
    internal fun extractNormalizedSaintFirstName(saintName: String): String {
        val withoutPrefix = saintName.trim().replace(SAINT_PREFIX_REGEX, "").trim()
        val normalizedFull = normalizeText(withoutPrefix)
        if (normalizedFull.isEmpty() || normalizedFull in NON_FIRST_NAME_CELEBRATIONS) {
            return ""
        }
        val withoutEpithet = withoutPrefix.replace(EPITHET_SUFFIX_REGEX, "").trim()
        return normalizeText(withoutEpithet)
    }

    /**
     * Découpe un nom normalisé en tokens de mots complets (en préservant les traits d'union internes aux prénoms composés).
     */
    internal fun tokenizeNormalizedName(normalized: String): List<String> {
        if (normalized.isBlank()) return emptyList()
        return WORD_TOKEN_REGEX.findAll(normalized).map { it.value }.toList()
    }

    /**
     * Vérifie si le nom d'un contact correspond exactement au prénom du saint (mot entier uniquement).
     * Exemple : "Clément" matche "Clément" et "Clément Dupont", mais NE matche PAS "Clémentine" ni "Jean-Clément".
     */
    internal fun doesContactMatchSaint(contactName: String, saintName: String): Boolean {
        val normalizedSaint = extractNormalizedSaintFirstName(saintName)
        if (normalizedSaint.isEmpty()) return false

        val saintTokens = tokenizeNormalizedName(normalizedSaint)
        if (saintTokens.isEmpty()) return false

        val normalizedContact = normalizeText(contactName)
        val contactTokens = tokenizeNormalizedName(normalizedContact)
        if (contactTokens.isEmpty()) return false

        // 1. Correspondance exacte de la séquence de tokens (ex: ["clement"] dans ["clement", "dupont"] ou ["jean-baptiste"] dans ["jean-baptiste", "martin"])
        if (containsTokenSequence(contactTokens, saintTokens)) {
            return true
        }

        // 2. Si le saint est un prénom composé avec trait d'union (ex: "Jean-Baptiste"),
        // accepter aussi le cas où le contact est écrit avec un espace ("Jean Baptiste")
        if (saintTokens.size == 1 && '-' in saintTokens[0]) {
            val splitCompoundTokens = saintTokens[0].split('-').filter { it.isNotEmpty() }
            if (splitCompoundTokens.size > 1 && containsTokenSequence(contactTokens, splitCompoundTokens)) {
                return true
            }
        }

        return false
    }

    private fun containsTokenSequence(haystack: List<String>, needle: List<String>): Boolean {
        if (needle.isEmpty() || needle.size > haystack.size) return false
        val limit = haystack.size - needle.size
        for (i in 0..limit) {
            var allMatch = true
            for (j in needle.indices) {
                if (haystack[i + j] != needle[j]) {
                    allMatch = false
                    break
                }
            }
            if (allMatch) return true
        }
        return false
    }

    /**
     * Indique si au moins un contact du répertoire correspond au prénom du saint donné.
     */
    fun isNameInContacts(saintName: String): Boolean {
        return getMatchingContacts(saintName).isNotEmpty()
    }

    /**
     * Retourne la liste distincte des noms de contacts correspondant exactement au prénom du saint.
     */
    fun getMatchingContacts(saintName: String): List<String> {
        val normalizedSaint = extractNormalizedSaintFirstName(saintName)
        if (normalizedSaint.isEmpty()) return emptyList()

        val matches = mutableListOf<String>()
        val uri = ContactsContract.Contacts.CONTENT_URI
        val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            while (it.moveToNext()) {
                if (nameIndex >= 0) {
                    val contactName = it.getString(nameIndex) ?: continue
                    if (doesContactMatchSaint(contactName, saintName)) {
                        matches.add(contactName)
                    }
                }
            }
        }
        return matches.distinct()
    }
}
