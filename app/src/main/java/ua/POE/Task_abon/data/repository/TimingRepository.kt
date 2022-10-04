package ua.POE.Task_abon.data.repository

import androidx.room.Query
import ua.POE.Task_abon.data.dao.TimingDao
import ua.POE.Task_abon.data.entities.Timing
import javax.inject.Inject

class TimingRepository @Inject constructor(private val timingDao: TimingDao) {

    fun getTiming(taskId: Int) = timingDao.getTiming(taskId)

    fun saveStartTaskDate(taskId: Int, num: String, date: String) = timingDao.updateStartTaskDate(taskId, num, date)

    fun updateFirstEditDate(taskId: Int, num:String, firstEditDate: String) = timingDao.updateFirstEditDate(taskId, num, firstEditDate)

    fun updateEditCount(taskId: Int, num:String, editCount: Int) = timingDao.updateEditCount(taskId, num, editCount)

    fun updateEditSeconds(taskId: Int, num:String, editSeconds: Int) = timingDao.updateEditSeconds(taskId, num, editSeconds)

    fun updateLastEditDate(taskId: Int, num:String, lastEditDate: String) = timingDao.updateLastEditDate(taskId, num, lastEditDate)

    fun getEditTime(taskId: Int, num:String) = timingDao.getEditTime(taskId, num)

    fun isStartTaskDateEmpty(taskId: Int, num:String): Boolean {
        return timingDao.getStartTaskDate(taskId, num).isNullOrEmpty()
    }

    fun isFirstEditDateEmpty(taskId: Int, num:String): Boolean {
        return timingDao.getFirstEditDate(taskId, num).isNullOrEmpty()
    }

    fun upEditCount(taskId: Int, num: String) = timingDao.upEditCount(taskId, num)

    suspend fun insertTiming(timing: Timing) = timingDao.insertTiming(timing)

    suspend fun deleteTiming(taskId: Int) = timingDao.deleteByTaskId(taskId)

}