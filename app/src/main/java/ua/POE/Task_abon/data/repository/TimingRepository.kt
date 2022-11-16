package ua.POE.Task_abon.data.repository

import ua.POE.Task_abon.data.dao.TimingDao
import ua.POE.Task_abon.data.entities.Timing
import javax.inject.Inject

class TimingRepository @Inject constructor(private val timingDao: TimingDao) {

    suspend fun getTiming(taskId: Int) = timingDao.getTiming(taskId)

    suspend fun updateFirstEditDate(taskId: Int, num: Int, firstEditDate: String) =
        timingDao.updateFirstEditDate(taskId, num, firstEditDate)

    suspend fun updateEditCount(taskId: Int, num: Int, editCount: Int) =
        timingDao.updateEditCount(taskId, num, editCount)

    suspend fun updateEditSeconds(taskId: Int, num: Int, editSeconds: Int) =
        timingDao.updateEditSeconds(taskId, num, editSeconds)

    suspend fun updateLastEditDate(taskId: Int, num: Int, lastEditDate: String) =
        timingDao.updateLastEditDate(taskId, num, lastEditDate)

    suspend fun getEditTime(taskId: Int, num: Int) = timingDao.getEditTime(taskId, num)

    suspend fun isStartTaskDateEmpty(taskId: Int, num: Int): Boolean {
        return timingDao.getStartTaskDate(taskId, num).isNullOrEmpty()
    }

    suspend fun isFirstEditDateEmpty(taskId: Int, num: Int): Boolean {
        return timingDao.getFirstEditDate(taskId, num).isNullOrEmpty()
    }

    suspend fun upEditCount(taskId: Int, num: Int) = timingDao.upEditCount(taskId, num)

    suspend fun insertTiming(timing: Timing) = timingDao.insertTiming(timing)

    suspend fun deleteTiming(taskId: Int) = timingDao.deleteByTaskId(taskId)

}