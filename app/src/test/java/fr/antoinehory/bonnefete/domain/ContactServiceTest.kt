package fr.antoinehory.bonnefete.domain

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
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
    fun `isNameInContacts should return true when count is greater than 0`() {
        // Given
        val name = "Basile"
        every {
            contentResolver.query(any(), any(), any(), any(), any())
        } returns cursor
        every { cursor.count } returns 1
        every { cursor.close() } returns Unit

        // When
        val result = contactService.isNameInContacts(name)

        // Then
        assertTrue(result)
        verify { cursor.close() }
    }

    @Test
    fun `isNameInContacts should return false when count is 0`() {
        // Given
        val name = "Basile"
        every {
            contentResolver.query(any(), any(), any(), any(), any())
        } returns cursor
        every { cursor.count } returns 0
        every { cursor.close() } returns Unit

        // When
        val result = contactService.isNameInContacts(name)

        // Then
        assertFalse(result)
    }

    @Test
    fun `getMatchingContacts should return list of distinct names`() {
        // Given
        val name = "Gene"
        every {
            contentResolver.query(any(), any(), any(), any(), any())
        } returns cursor
        
        every { cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME) } returns 0
        every { cursor.moveToNext() } returnsMany listOf(true, true, false)
        every { cursor.getString(0) } returnsMany listOf("Geneviève", "Geneviève")
        every { cursor.close() } returns Unit

        // When
        val result = contactService.getMatchingContacts(name)

        // Then
        assertEquals(1, result.size)
        assertEquals("Geneviève", result[0])
    }
}
