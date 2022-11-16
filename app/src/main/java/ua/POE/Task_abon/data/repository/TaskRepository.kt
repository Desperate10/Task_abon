package ua.POE.Task_abon.data.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import ua.POE.Task_abon.data.dao.TaskDao
import ua.POE.Task_abon.data.entities.Result
import ua.POE.Task_abon.data.entities.TaskEntity
import ua.POE.Task_abon.data.entities.Timing
import ua.POE.Task_abon.domain.model.TaskInfo
import ua.POE.Task_abon.utils.Resource
import ua.POE.Task_abon.utils.XmlLoader
import ua.POE.Task_abon.utils.saveReadFile
import javax.inject.Inject

class TaskRepository @Inject constructor(private val xmlLoader: XmlLoader, private val taskDao: TaskDao)  {


    fun getTasks(): Flow<List<TaskEntity>>  = taskDao.getAll()

    suspend fun getTask(taskId: Int) = taskDao.getTask(taskId)

    suspend fun readFile(uri : Uri) = saveReadFile { xmlLoader.readXml(uri) }

    suspend fun createXml(results : List<Result>, timings: List<Timing>) = xmlLoader.createXml(results, timings)

    suspend fun deleteByTaskId(taskId: Int) = taskDao.deleteById(taskId)




}