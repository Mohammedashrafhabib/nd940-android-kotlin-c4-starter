package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    private  var remindersData = mutableListOf<ReminderDTO>()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        try {

            return Result.Success(remindersData.toList())
        }catch (ex:Exception){
            return Result.Error(ex.localizedMessage)
        }    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersData.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        try {
            val ans= remindersData.find { it.id==id } ?: return Result.Error("Reminder not found!")
            return Result.Success(ans)
        }catch (ex:Exception){
            return Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun deleteAllReminders() {
        remindersData.clear()
    }
    fun dataSourceClear(){
        remindersData.clear()

    }


}