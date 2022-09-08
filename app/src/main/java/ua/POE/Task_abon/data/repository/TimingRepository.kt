package ua.POE.Task_abon.data.repository

import androidx.room.Query
import ua.POE.Task_abon.data.dao.TimingDao
import ua.POE.Task_abon.data.entities.Timing
import javax.inject.Inject

class TimingRepository @Inject constructor(private val timingDao: TimingDao) {

    fun getTiming(taskId: String) = timingDao.getTiming(taskId)

    fun saveStartTaskDate(taskId: String, date: String) = timingDao.updateStartTaskDate(taskId, date)

    fun updateFirstEditDate(taskId: String, firstEditDate: String) = timingDao.updateFirstEditDate(taskId, firstEditDate)

    fun updateEditCount(taskId: String, editCount: Int) = timingDao.updateEditCount(taskId, editCount)

    fun updateEditSeconds(taskId: String, editSeconds: Int) = timingDao.updateEditSeconds(taskId, editSeconds)

    fun updateLastEditDate(taskId: String, lastEditDate: String) = timingDao.updateLastEditDate(taskId, lastEditDate)

    fun getEditTime(taskId: String) = timingDao.getEditTime(taskId)

    fun isStartTaskDateEmpty(taskId: String): Boolean {
        return timingDao.getStartTaskDate(taskId).isNullOrEmpty()
    }

    fun isFirstEditDateEmpty(taskId: String): Boolean {
        return timingDao.getFirstEditDate(taskId).isNullOrEmpty()
    }

    fun upEditCount(taskId: String) = timingDao.upEditCount(taskId)

    suspend fun insertTiming(timing: Timing) = timingDao.insertTiming(timing)

}