package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var remindersDatabase: RemindersDatabase
    @Before
    fun init() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }
    @After
    fun closeDataBase() {
        remindersDatabase.close()
    }
    //add task to database then check if saved correctly
    @Test
    fun addTaskAndCheckDataBase()=runBlockingTest{
        val reminder=  ReminderDTO("reminder","test1","location1",565.33,565.33)
        val reminder2=  ReminderDTO("reminder2","test2","location2",565.33,565.33)
        val reminder3=  ReminderDTO("reminder3","test3","location3",565.33,565.33)
        remindersDatabase.reminderDao().saveReminder(reminder)
        remindersDatabase.reminderDao().saveReminder(reminder2)
        remindersDatabase.reminderDao().saveReminder(reminder3)
        val list=remindersDatabase.reminderDao().getReminders()
        Assert.assertThat(list.size, `is`(3))
        var remindertest=remindersDatabase.reminderDao().getReminderById(reminder.id)
        Assert.assertThat<ReminderDTO>(remindertest as ReminderDTO, notNullValue())
        Assert.assertThat(remindertest.id, `is`(reminder.id))
        Assert.assertThat(remindertest.title, `is`(reminder.title))
        Assert.assertThat(remindertest.description, `is`(reminder.description))
        Assert.assertThat(remindertest.location, `is`(reminder.location))
        Assert.assertThat(remindertest.latitude, `is`(reminder.latitude))
        Assert.assertThat(remindertest.longitude, `is`(reminder.longitude))
        remindertest=remindersDatabase.reminderDao().getReminderById(reminder2.id)
        Assert.assertThat<ReminderDTO>(remindertest as ReminderDTO, notNullValue())
        Assert.assertThat(remindertest.id, `is`(reminder2.id))
        Assert.assertThat(remindertest.title, `is`(reminder2.title))
        Assert.assertThat(remindertest.description, `is`(reminder2.description))
        Assert.assertThat(remindertest.location, `is`(reminder2.location))
        Assert.assertThat(remindertest.latitude, `is`(reminder2.latitude))
        Assert.assertThat(remindertest.longitude, `is`(reminder2.longitude))
        remindertest=remindersDatabase.reminderDao().getReminderById(reminder3.id)
        Assert.assertThat<ReminderDTO>(remindertest as ReminderDTO, notNullValue())
        Assert.assertThat(remindertest.id, `is`(reminder3.id))
        Assert.assertThat(remindertest.title, `is`(reminder3.title))
        Assert.assertThat(remindertest.description, `is`(reminder3.description))
        Assert.assertThat(remindertest.location, `is`(reminder3.location))
        Assert.assertThat(remindertest.latitude, `is`(reminder3.latitude))
        Assert.assertThat(remindertest.longitude, `is`(reminder3.longitude))
    }
}