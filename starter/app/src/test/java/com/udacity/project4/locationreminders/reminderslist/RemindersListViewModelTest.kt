package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var reminderDataSource: ReminderDataSource
    private lateinit var ReminderListViewModel: RemindersListViewModel
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    @Before
    fun setUp() {
        this.reminderDataSource = FakeDataSource()
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        this.ReminderListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            this.reminderDataSource
        )
    }
    @After
    fun tearDown() {
        stopKoin()
        (reminderDataSource as FakeDataSource).dataSourceClear()
    }

    @Test
    fun loadRemindersShouldHideLoadingWhenMethodIsFinished() {

        ReminderListViewModel.loadReminders()

        assertEquals(false, ReminderListViewModel.showLoading.value)
    }

    @Test
    fun loadRemindersShouldShowLoadingWhenMethodIsCalled() {
        mainCoroutineRule.pauseDispatcher()

        ReminderListViewModel.loadReminders()

        assertTrue(ReminderListViewModel.showLoading.value == true)
        mainCoroutineRule.resumeDispatcher()
        assertEquals(false, ReminderListViewModel.showLoading.value)

    }

}