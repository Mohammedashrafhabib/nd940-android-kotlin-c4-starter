package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersLocalRepository:RemindersLocalRepository
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    @Before
    fun init() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        remindersLocalRepository= RemindersLocalRepository(remindersDatabase.reminderDao(),Dispatchers.IO)
    }
    @After
    fun closeDataBase() {
        remindersDatabase.close()
    }
    @Test
    fun addTaskAndCheckRepository()= runBlocking{
            val reminder=  ReminderDTO("reminder","test1","location1",565.33,565.33)
            val reminder2=  ReminderDTO("reminder2","test2","location2",565.33,565.33)
            remindersLocalRepository.saveReminder(reminder)
            remindersLocalRepository.saveReminder(reminder2)
        val result=remindersLocalRepository.getReminders()
        val result1=remindersLocalRepository.getReminder(reminder.id)
        val reminderTest=(result1 as Result.Success).data
        Assert.assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        Assert.assertThat(result.data.size, `is`(2))
        Assert.assertThat(result1 is Result.Success, `is`(true))
        Assert.assertThat(reminderTest.title, `is`(reminder.title))
        Assert.assertThat(reminderTest.description, `is`(reminder.description))
        Assert.assertThat(reminderTest.location, `is`(reminder.location))
        Assert.assertThat(reminderTest.latitude, `is`(reminder.latitude))
        Assert.assertThat(reminderTest.longitude, `is`(reminder.longitude))
        remindersLocalRepository.deleteAllReminders()
        val result2=remindersLocalRepository.getReminder(reminder.id)
        Assert.assertThat(result2 is Result.Error, `is`(true))
        result2 as Result.Error
        Assert.assertThat(result2.message , `is`("Reminder not found!"))
    }


}