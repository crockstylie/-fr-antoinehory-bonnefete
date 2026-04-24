package fr.antoinehory.bonnefete.ui

import fr.antoinehory.bonnefete.data.repository.SaintRepository
import fr.antoinehory.bonnefete.data.repository.UserPreferencesRepository
import fr.antoinehory.bonnefete.domain.WorkScheduler
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val saintRepository = mockk<SaintRepository>()
    private val preferencesRepository = mockk<UserPreferencesRepository>()
    private val workScheduler = mockk<WorkScheduler>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { saintRepository.populateDatabaseIfNeeded() } returns Unit
        every { preferencesRepository.notificationTime } returns flowOf(Pair(9, 0))
        every { preferencesRepository.onlyContacts } returns flowOf(true)
        every { saintRepository.getSaintForDate(any(), any()) } returns flowOf(null)
        every { workScheduler.scheduleDailyUpdate(any(), any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel should initialize with correct state`() {
        // When
        val viewModel = MainViewModel(saintRepository, preferencesRepository, workScheduler)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(9, viewModel.uiState.value.notificationHour)
        assertEquals(true, viewModel.uiState.value.onlyContacts)
    }
}
