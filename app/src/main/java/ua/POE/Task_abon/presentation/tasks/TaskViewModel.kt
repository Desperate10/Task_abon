package ua.POE.Task_abon.presentation.tasks

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.POE.Task_abon.data.dao.ResultDao
import ua.POE.Task_abon.data.entities.TaskEntity
import ua.POE.Task_abon.data.mapper.toTaskInfo
import ua.POE.Task_abon.data.repository.DirectoryRepository
import ua.POE.Task_abon.data.repository.TaskRepository
import ua.POE.Task_abon.data.repository.TestEntityRepository
import ua.POE.Task_abon.data.repository.TimingRepository
import ua.POE.Task_abon.domain.model.TaskInfo
import ua.POE.Task_abon.utils.Resource

class TaskViewModel @ViewModelInject constructor(private val repository: TaskRepository, private val testEntityRepository: TestEntityRepository, private val taskRepository: TaskRepository, private val directoryRepository: DirectoryRepository, val resultDao: ResultDao, private val timingRepository: TimingRepository) : ViewModel() {


    val tasks : Flow<List<TaskInfo>> = repository.getTasks().map { listOfTasks ->
        listOfTasks.map { taskEntity ->
            taskEntity.toTaskInfo()
        }
    }.flowOn(Dispatchers.IO)

    fun insert(uri: Uri) = viewModelScope.launch {
            repository.readFile(uri)
    }

    fun clearTaskData(taskId: Int) = viewModelScope.launch {
        testEntityRepository.setUnDone(taskId)
        resultDao.delete(taskId)
        timingRepository.deleteTiming(taskId)
    }

    fun deleteTask(taskId: Int) = viewModelScope.launch {
        taskRepository.deleteByTaskId(taskId)
        testEntityRepository.deleteTable(taskId)
        directoryRepository.deleteDirectoryByTaskId(taskId)
        resultDao.delete(taskId)
        timingRepository.deleteTiming(taskId)
        repository.deleteByTaskId(taskId)
    }

    private suspend fun getResult(taskId: Int) = resultDao.getResultByTaskId(taskId)

    suspend fun getPhotos(taskId: Int) : List<String> = resultDao.getAllPhotos(taskId)

    suspend fun createXml(taskId: Int) :String {
        val result = getResult(taskId)
        val timing = getTiming(taskId)
        return repository.createXml(result, timing)
    }

    private fun getTiming(taskId: Int) = timingRepository.getTiming(taskId)


}