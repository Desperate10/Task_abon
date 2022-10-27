package ua.POE.Task_abon.data.repository

import androidx.room.Query
import ua.POE.Task_abon.data.dao.TimingDao
import ua.POE.Task_abon.data.entities.Timing
import javax.inject.Inject

class TimingRepository @Inject constructor(private val timingDao: TimingDao) {

    fun getTiming(taskId: Int) = timingDao.getTiming(taskId)

    fun saveStartTaskDate(taskId: Int, num: Int, date: String) = timingDao.updateStartTaskDate(taskId, num, date)

    fun updateFirstEditDate(taskId: Int, num: Int, firstEditDate: String) = timingDao.updateFirstEditDate(taskId, num, firstEditDate)

    fun updateEditCount(taskId: Int, num: Int, editCount: Int) = timingDao.updateEditCount(taskId, num, editCount)

    fun updateEditSeconds(taskId: Int, num: Int, editSeconds: Int) = timingDao.updateEditSeconds(taskId, num, editSeconds)

    fun updateLastEditDate(taskId: Int, num: Int, lastEditDate: String) = timingDao.updateLastEditDate(taskId, num, lastEditDate)

    fun getEditTime(taskId: Int, num: Int) = timingDao.getEditTime(taskId, num)

    fun isStartTaskDateEmpty(taskId: Int, num:Int): Boolean {
        return timingDao.getStartTaskDate(taskId, num).isNullOrEmpty()
    }

    fun isFirstEditDateEmpty(taskId: Int, num: Int): Boolean {
        return timingDao.getFirstEditDate(taskId, num).isNullOrEmpty()
    }

    fun upEditCount(taskId: Int, num: Int) = timingDao.upEditCount(taskId, num)

    suspend fun insertTiming(timing: Timing) = timingDao.insertTiming(timing)

    suspend fun deleteTiming(taskId: Int) = timingDao.deleteByTaskId(taskId)

}