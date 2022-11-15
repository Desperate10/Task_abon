package ua.POE.Task_abon.presentation.task

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.POE.Task_abon.data.dao.*
import ua.POE.Task_abon.data.dao.impl.TaskCustomerDaoImpl
import ua.POE.Task_abon.data.entities.Result
import ua.POE.Task_abon.data.entities.Timing
import ua.POE.Task_abon.data.mapper.toTaskInfo
import ua.POE.Task_abon.domain.model.TaskInfo
import ua.POE.Task_abon.utils.XmlLoader
import ua.POE.Task_abon.utils.mapLatestIterable
import ua.POE.Task_abon.utils.saveReadFile
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val dynamicTaskData: TaskCustomerDaoImpl,
    private val task: TaskDao,
    private val directory: DirectoryDao,
    private val result: ResultDao,
    private val timing: TimingDao,
    private val xmlLoader: XmlLoader
) : ViewModel() {

    val tasks: Flow<List<TaskInfo>> =
        task.getAll().mapLatestIterable {
            it.toTaskInfo()
        }
            .flowOn(Dispatchers.IO)
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 2)

    //на возврат функции можно прикрутить sealed class и stateflow
    fun insert(uri: Uri) = viewModelScope.launch {
        readFile(uri)
    }

    fun clearTaskData(taskId: Int) = viewModelScope.launch {
        dynamicTaskData.setUnDone(taskId)
        result.delete(taskId)
        timing.deleteByTaskId(taskId)
    }

    fun deleteTask(taskId: Int) = viewModelScope.launch {
        task.deleteById(taskId)
        dynamicTaskData.deleteTable(taskId)
        directory.deleteDirectoryByTaskId(taskId)
        result.delete(taskId)
        timing.deleteByTaskId(taskId)
        task.deleteById(taskId)
    }

    suspend fun createXml(taskId: Int): String {
        val result = getResult(taskId)
        val timing = getTiming(taskId)
        return createXml(result, timing)
    }

    suspend fun getPhotos(taskId: Int): List<String> = result.getAllPhotos(taskId)

    private suspend fun getTiming(taskId: Int) = timing.getTiming(taskId)

    private suspend fun getResult(taskId: Int) = result.getResultByTaskId(taskId)

    private suspend fun readFile(uri : Uri) = saveReadFile { xmlLoader.readXml(uri) }

    suspend fun createXml(results : List<Result>, timings: List<Timing>) = xmlLoader.createXml(results, timings)


}