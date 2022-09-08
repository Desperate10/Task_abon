package ua.POE.Task_abon.data.repository

import androidx.lifecycle.asLiveData
import ua.POE.Task_abon.data.dao.ResultDao
import javax.inject.Inject

class ResultRepository @Inject constructor(private val resultDao: ResultDao) {

    suspend fun getResultTask(taskId: String) = resultDao.getResultByTaskId(taskId)

    suspend fun deleteResults(taskId: String) = resultDao.delete(taskId)

}