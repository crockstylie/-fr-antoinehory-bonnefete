package fr.antoinehory.bonnefete.domain

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ContactServiceTest {

    private val context = mockk<Context>()
    private val contentResolver = mockk<ContentResolver>()
    private val cursor = mockk<Cursor>()
    private lateinit var contactService: ContactService

    @Before
    fun setup() {
        every { context.contentResolver } returns contentResolver
        contactService = ContactService(context)
    }

    @Test
    fun `doesContactMatchSaint should match Clement but NEVER Clementine`() {
        assertTrue(contactService.doesContactMatchSaint("Clément", "Clément"))
        assertTrue(contactService.doesContactMatchSaint("clement dupont", "Clément"))
        assertTrue(contactService.doesContactMatchSaint("Dupont Clément", "Saint Clément"))

        // False-positive regression checks
        assertFalse(contactService.doesContactMatchSaint("Clémentine", "Clément"))
        assertFalse(contactService.doesContactMatchSaint("Clémentine Dupont", "Clément"))
        assertFalse(contactService.doesContactMatchSaint("Jean-Clément", "Clément"))
    }

    @Test
    fun `doesContactMatchSaint should handle compound names and epithets accurately`() {
        assertTrue(contactService.doesContactMatchSaint("Jean-Baptiste Martin", "Jean-Baptiste"))
        assertTrue(contactService.doesContactMatchSaint("Jean Baptiste Martin", "Jean-Baptiste"))
        assertFalse(contactService.doesContactMatchSaint("Jean Martin", "Jean-Baptiste"))
        assertFalse(contactService.doesContactMatchSaint("Jean-Baptiste Martin", "Jean"))

        assertTrue(contactService.doesContactMatchSaint("François Dupont", "François d'Assise"))
        assertTrue(contactService.doesContactMatchSaint("Antoine Hory", "Antoine de Padoue"))
        assertFalse(contactService.doesContactMatchSaint("Jour", "Jour de l'An"))
    }

    @Test
    fun `isNameInContacts should return true when a matching contact exists`() {
        // Given
        val name = "Basile"
        every {
            contentResolver.query(any(), any(), any(), any(), any())
        } returns cursor
        every { cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME) } returns 0
        every { cursor.moveToNext() } returnsMany listOf(true, false)
        every { cursor.getString(0) } returns "Basile Dupont"
        every { cursor.close() } returns Unit

        // When
        val result = contactService.isNameInContacts(name)

        // Then
        assertTrue(result)
        verify { cursor.close() }
    }

    @Test
    fun `isNameInContacts should return false when only substring match exists`() {
        // Given
        val name = "Clément"
        every {
            contentResolver.query(any(), any(), any(), any(), any())
        } returns cursor
        every { cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME) } returns 0
        every { cursor.moveToNext() } returnsMany listOf(true, false)
        every { cursor.getString(0) } returns "Clémentine"
        every { cursor.close() } returns Unit

        // When
        val result = contactService.isNameInContacts(name)

        // Then
        assertFalse(result)
        verify { cursor.close() }
    }

    @Test
    fun `getMatchingContacts should return list of distinct exact matching names`() {
        // Given
        val name = "Geneviève"
        every {
            contentResolver.query(any(), any(), any(), any(), any())
        } returns cursor

        every { cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME) } returns 0
        every { cursor.moveToNext() } returnsMany listOf(true, true, true, false)
        every { cursor.getString(0) } returnsMany listOf("Geneviève", "Geneviève", "Geneviève2")
        every { cursor.close() } returns Unit

        // When
        val result = contactService.getMatchingContacts(name)

        // Then
        assertEquals(1, result.size)
        assertEquals("Geneviève", result[0])
    }
}
