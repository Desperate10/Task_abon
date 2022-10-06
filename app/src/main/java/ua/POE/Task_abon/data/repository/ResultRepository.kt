package ua.POE.Task_abon.data.repository

import androidx.lifecycle.asLiveData
import ua.POE.Task_abon.data.dao.ResultDao
import javax.inject.Inject

class ResultRepository @Inject constructor(private val resultDao: ResultDao) {

    suspend fun getResultTask(taskId: Int) = resultDao.getResultByTaskId(taskId)

    suspend fun deleteResults(taskId: Int) = resultDao.delete(taskId)

}