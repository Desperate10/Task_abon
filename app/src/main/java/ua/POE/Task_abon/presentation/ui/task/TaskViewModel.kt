package ua.POE.Task_abon.presentation.ui.task

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.POE.Task_abon.data.dao.DirectoryDao
import ua.POE.Task_abon.data.dao.ResultDao
import ua.POE.Task_abon.data.dao.TaskDao
import ua.POE.Task_abon.data.dao.TimingDao
import ua.POE.Task_abon.data.dao.impl.TaskCustomerDaoImpl
import ua.POE.Task_abon.data.entities.Result
import ua.POE.Task_abon.data.entities.Timing
import ua.POE.Task_abon.data.mapper.toTaskInfo
import ua.POE.Task_abon.domain.model.TaskInfo
import ua.POE.Task_abon.network.UploadWorker
import ua.POE.Task_abon.data.xml.XmlRead
import ua.POE.Task_abon.data.xml.XmlWrite
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
    private val xmlRead: XmlRead,
    private val xmlWrite: XmlWrite,
    private val workManager: WorkManager,
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

    fun uploadImagesRequestBuilder(taskId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val photoUris = getPhotos(taskId)
                workManager.enqueueUniqueWork(
                    UploadWorker.WORK_NAME,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    UploadWorker.makeRequest(photoUris.toTypedArray())
                )
            }
        }
    }

    private suspend fun getPhotos(taskId: Int): List<String> = result.getAllPhotos(taskId)

    private suspend fun getTiming(taskId: Int) = timing.getTiming(taskId)

    private suspend fun getResult(taskId: Int) = result.getResultByTaskId(taskId)

    private suspend fun readFile(uri: Uri) = saveReadFile { xmlRead(uri) }

    private suspend fun createXml(results: List<Result>, timings: List<Timing>) =
        xmlWrite(results, timings)

}