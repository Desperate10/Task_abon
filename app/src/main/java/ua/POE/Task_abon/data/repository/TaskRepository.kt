package ua.POE.Task_abon.data.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ua.POE.Task_abon.data.dao.TaskDao
import ua.POE.Task_abon.data.dao.UserDataDao
import ua.POE.Task_abon.data.entities.Result
import ua.POE.Task_abon.data.entities.Task
import ua.POE.Task_abon.data.entities.Timing
import ua.POE.Task_abon.data.entities.UserData
import ua.POE.Task_abon.utils.XmlLoader
import javax.inject.Inject

class TaskRepository @Inject constructor(private val xmlLoader: XmlLoader, private val taskDao: TaskDao) :
    BaseRepository() {


    fun getTasks() : Flow<List<Task>> = taskDao.getAll()

    suspend fun getTask(taskId: String) = taskDao.getTask(taskId)

    suspend fun readFile(uri : Uri) = saveReadFile { xmlLoader.readXml(uri) }

    suspend fun createXml(results : List<Result>, timings: List<Timing>) = xmlLoader.createXml(results, timings)

    suspend fun deleteByTaskId(taskId: String) = taskDao.deleteById(taskId)




}