package fr.antoinehory.bonnefete.data.repository

import android.content.Context
import android.content.res.AssetManager
import fr.antoinehory.bonnefete.data.local.dao.SaintDao
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class SaintRepositoryTest {

    private val saintDao = mockk<SaintDao>()
    private val context = mockk<Context>()
    private val assetManager = mockk<AssetManager>()
    private lateinit var repository: SaintRepository

    @Before
    fun setup() {
        every { context.assets } returns assetManager
        repository = SaintRepository(saintDao, context)
    }

    @Test
    fun `populateDatabaseIfNeeded should insert saints when database is empty`() = runTest {
        // Given
        coEvery { saintDao.getCount() } returns 0
        val json = """
            {
              "january": [["Basile", "Saint"]]
            }
        """.trimIndent()
        every { assetManager.open("saints.json") } returns ByteArrayInputStream(json.toByteArray())
        coEvery { saintDao.insertSaints(any()) } returns Unit

        // When
        repository.populateDatabaseIfNeeded()

        // Then
        verify { assetManager.open("saints.json") }
        coEvery { saintDao.insertSaints(match { it.size == 1 && it[0].name == "Basile" }) }
    }

    @Test
    fun `populateDatabaseIfNeeded should not insert when database is not empty`() = runTest {
        // Given
        coEvery { saintDao.getCount() } returns 12

        // When
        repository.populateDatabaseIfNeeded()

        // Then
        verify(exactly = 0) { assetManager.open(any()) }
    }
}
