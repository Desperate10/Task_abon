package ua.POE.Task_abon.presentation.tasks

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.POE.Task_abon.data.dao.CatalogDao
import ua.POE.Task_abon.data.dao.ResultDao
import ua.POE.Task_abon.data.mapper.toTaskInfo
import ua.POE.Task_abon.data.repository.DirectoryRepository
import ua.POE.Task_abon.data.repository.TaskRepository
import ua.POE.Task_abon.data.repository.TestEntityRepository
import ua.POE.Task_abon.data.repository.TimingRepository
import ua.POE.Task_abon.domain.model.TaskInfo
import ua.POE.Task_abon.utils.mapLatestIterable
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val testEntityRepository: TestEntityRepository,
    private val taskRepository: TaskRepository,
    private val directoryRepository: DirectoryRepository,
    private val resultDao: ResultDao,
    private val timingRepository: TimingRepository
) : ViewModel() {

    val tasks: Flow<List<TaskInfo>> =
        repository.getTasks().mapLatestIterable {
            it.toTaskInfo()
        }
            .flowOn(Dispatchers.IO)
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 2)

    //на возврат функции можно прикрутить sealed class и stateflow
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

    suspend fun getPhotos(taskId: Int): List<String> = resultDao.getAllPhotos(taskId)

    suspend fun createXml(taskId: Int): String {
        val result = getResult(taskId)
        val timing = getTiming(taskId)
        return repository.createXml(result, timing)
    }

    private suspend fun getTiming(taskId: Int) = timingRepository.getTiming(taskId)


}