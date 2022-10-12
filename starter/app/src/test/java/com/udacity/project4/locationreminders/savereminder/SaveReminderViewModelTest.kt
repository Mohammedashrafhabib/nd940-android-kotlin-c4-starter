package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var reminderDataSource: ReminderDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    @Before
    fun setUp() {
        this.reminderDataSource =FakeDataSource()
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        this.saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            this.reminderDataSource
        )
    }
    @After
    fun tearDown() {
        stopKoin()
        (reminderDataSource as FakeDataSource).dataSourceClear()
    }
//input data in livedata then clearing livedata and checking its null
    @Test
    fun onClearShouldClearReminderData() {
        val reminderDescription = "description"
        val reminderSelectedLocationStr = "location"
        val latitude =1.0
        val longitude = 1.0
        val reminderTitle = "title"



        saveReminderViewModel.reminderDescription.value = reminderDescription
        saveReminderViewModel.reminderSelectedLocationStr.value = reminderSelectedLocationStr
        saveReminderViewModel.latitude.value = latitude
        saveReminderViewModel.longitude.value = longitude
        saveReminderViewModel.reminderTitle.value = reminderTitle

        saveReminderViewModel.onClear()

        Assert.assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue() ,CoreMatchers.nullValue())
        Assert.assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue() ,CoreMatchers.nullValue())
        Assert.assertThat(saveReminderViewModel.latitude.getOrAwaitValue() ,CoreMatchers.nullValue())
        Assert.assertThat(saveReminderViewModel.longitude.getOrAwaitValue(),CoreMatchers.nullValue())
        Assert.assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(),CoreMatchers.nullValue())

    }
//input everything valid except title is empty check snackbar
    @Test
    fun validateAndSaveReminderWhenTitleIsEmptyThenShowSnackBarWithErrorMessage() {
        val reminderData = ReminderDataItem(
            title = "",
            description = "description",
            location = "location",
            latitude = 1.0,
            longitude = 1.0
        )
        val mockObserver = mock(Observer::class.java) as Observer<Int>
        saveReminderViewModel.showSnackBarInt.observeForever(mockObserver)

        saveReminderViewModel.validateAndSaveReminder(reminderData)

        verify(mockObserver).onChanged(R.string.err_enter_title)
    }
//input everything valid check snackbar
    @Test
    fun validateAndSaveReminderWhenTitleIsNotEmptyAndLocationIsNotEmptyThenSaveReminder() {
        val reminderData = ReminderDataItem(
            "title",
            "description",
            "location",
            1.0,
            2.0
        )

        val mockObserver = mock(Observer::class.java) as Observer<String>
        saveReminderViewModel.showToast.observeForever(mockObserver)
        saveReminderViewModel.validateAndSaveReminder(reminderData)
        verify(mockObserver).onChanged("Reminder Saved !")

    }
    //input everything valid except location is empty

    @Test
    fun validateEnteredDataWhenLocationIsEmptyThenReturnFalse() {
        val reminderData = ReminderDataItem(
            "title",
            "description",
            "",
            0.0,
            0.0
        )
        val result = saveReminderViewModel.validateEnteredData(reminderData)
        Assert.assertThat(result,CoreMatchers.`is` (false))
    }
//input everything valid except title is empty

    @Test
    fun validateEnteredDataWhenTitleIsEmptyThenReturnFalse() {
        val reminderData = ReminderDataItem(
            title = "",
            description = "description",
            location = "location",
            latitude = 0.0,
            longitude = 0.0
        )
        val result = saveReminderViewModel.validateEnteredData(reminderData)
        Assert.assertThat(result,CoreMatchers.`is` (false))
    }
//test saving to datasource
    @Test
    fun saveReminderWhenMethodIsCalledThenCallDataSourceSaveReminder() {
        val reminderData = ReminderDataItem(
            "title",
            "description",
            "location",
            1.0,
            2.0
        )
        this.saveReminderViewModel.saveReminder(reminderData)
        runBlockingTest {
            val res = reminderDataSource.getReminder(reminderData.id)
            Assert.assertThat(res is Result.Success<ReminderDTO>,CoreMatchers.`is`(true) )
        }

    }
    // check lodaing while saving and check toast and check navigationCommand
    @Test
    fun saveReminderWhenMethodIsCalledThenShowLoading() {
        val reminderData = ReminderDataItem(
            "title",
            "description",
            "location",
            1.0,
            2.0
        )
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminderData)
        Assert.assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(),CoreMatchers.`is` (true))
        mainCoroutineRule.resumeDispatcher()
        Assert.assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(),CoreMatchers.`is` (false))
        Assert.assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), CoreMatchers.`is`("Reminder Saved !"))
        Assert.assertThat<NavigationCommand>(saveReminderViewModel.navigationCommand.getOrAwaitValue(), CoreMatchers.`is`(NavigationCommand.Back))

    }
}