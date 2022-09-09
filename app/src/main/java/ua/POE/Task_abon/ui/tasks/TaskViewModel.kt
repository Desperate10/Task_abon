package ua.POE.Task_abon.ui.tasks

import android.net.Uri
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ua.POE.Task_abon.data.dao.ResultDao
import ua.POE.Task_abon.data.dao.TaskDao
import ua.POE.Task_abon.data.entities.Task
import ua.POE.Task_abon.data.entities.TestEntity
import ua.POE.Task_abon.data.repository.DirectoryRepository
import ua.POE.Task_abon.data.repository.TaskRepository
import ua.POE.Task_abon.data.repository.TestEntityRepository
import ua.POE.Task_abon.data.repository.TimingRepository
import ua.POE.Task_abon.utils.Resource
import ua.POE.Task_abon.utils.XmlLoader

class TaskViewModel @ViewModelInject constructor(private val repository: TaskRepository, private val testEntityRepository: TestEntityRepository, private val taskRepository: TaskRepository, private val directoryRepository: DirectoryRepository, val resultDao: ResultDao, private val timingRepository: TimingRepository) : ViewModel() {


    val tasks : LiveData<List<Task>> = repository.getTasks().asLiveData()


    private val _taskLoading : MutableLiveData<Resource<String>> = MutableLiveData()
    val taskLoadingStatus: LiveData<Resource<String>>
        get() = _taskLoading

    fun insert(uri: Uri) = viewModelScope.launch {
            repository.readFile(uri)
    }

    fun clearTaskData(taskId: String) = viewModelScope.launch {
        resultDao.delete(taskId)
    }

    fun deleteTask(taskId: String) = viewModelScope.launch {
        taskRepository.deleteByTaskId(taskId)
        testEntityRepository.deleteTable(taskId)
        directoryRepository.deleteDirectoryByTaskId(taskId)
        resultDao.delete(taskId)
        repository.deleteByTaskId(taskId)
    }

    private fun getResult(taskId: String) = resultDao.getResultByTaskId(taskId)

    fun getPhotos(taskId: String) : List<String> = resultDao.getAllPhotos(taskId)

    suspend fun createXml(taskId: String) :String {
        val result = getResult(taskId)
        val timing = getTiming(taskId)
        return repository.createXml(result, timing)
    }

    private fun getTiming(taskId: String) = timingRepository.getTiming(taskId)


}