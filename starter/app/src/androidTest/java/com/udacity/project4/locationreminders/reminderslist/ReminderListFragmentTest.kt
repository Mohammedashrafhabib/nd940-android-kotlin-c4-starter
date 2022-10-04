package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest:KoinTest {

    private val bindingIdlingResource = DataBindingIdlingResource()
    private lateinit var reminderDataSource: ReminderDataSource
    @Before
    fun init() {
        stopKoin()
        val appContext: Application = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        startKoin {
            modules(listOf(myModule))
        }
        reminderDataSource = get()

        runBlocking {
            reminderDataSource.deleteAllReminders()
        }
    }

    @Before
    fun registerBinding() {
        IdlingRegistry.getInstance().register(bindingIdlingResource)
    }
    @After
    fun unregisterBinding() {
        IdlingRegistry.getInstance().unregister(bindingIdlingResource)
    }
    @Test
    fun navigateToAddNewReminder()= runBlocking{

        reminderDataSource.saveReminder(
            ReminderDTO("Reminder", "test",
            "testLocation", 5.22,39.252)
        )
        reminderDataSource.saveReminder(ReminderDTO("Reminder2", "test2",
            "testLocation2", 22.22,55.33))
        val fragmentTest = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        fragmentTest.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        bindingIdlingResource.monitorFragment(fragmentTest)
        onView(withText("Reminder")).check(matches(isDisplayed()))
        onView(withText("Reminder2")).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
    @Test
    fun emptyDataBaseScenario(){
        val fragmentTest = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        fragmentTest.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        bindingIdlingResource.monitorFragment(fragmentTest)
        onView(withText("No Data")).check(matches(isDisplayed()))

    }
}