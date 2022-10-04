package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    private  var remindersData = mutableListOf<ReminderDTO>()
    private var databaseError = false
    fun databaseError(boolean: Boolean){
        databaseError=boolean
    }
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        try {
            if (databaseError){
                return Result.Error("There is an error in database")
            }
            return Result.Success(remindersData.toList())
        }catch (ex:Exception){
            return Result.Error(ex.message)
        }    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersData.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        try {
            val ans=remindersData.find { it.id==id }
            if(ans==null||databaseError){
                return Result.Error("Data is not found")
            }
            return Result.Success(ans)
        }catch (ex:Exception){
            return Result.Error(ex.message)
        }
    }

    override suspend fun deleteAllReminders() {
        remindersData.clear()
    }
    fun dataSourceClear(){
        remindersData.clear()

    }


}