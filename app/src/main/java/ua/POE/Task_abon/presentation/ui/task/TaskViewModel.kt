package ua.POE.Task_abon.presentation.ui.task

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.POE.Task_abon.data.dao.DirectoryDao
import ua.POE.Task_abon.data.dao.ResultDao
import ua.POE.Task_abon.data.dao.TaskDao
import ua.POE.Task_abon.data.dao.TimingDao
import ua.POE.Task_abon.data.dao.TaskDataDaoImpl
import ua.POE.Task_abon.data.mapper.toTaskInfo
import ua.POE.Task_abon.data.xml.XmlRead
import ua.POE.Task_abon.data.xml.XmlWrite
import ua.POE.Task_abon.network.UploadWorker
import ua.POE.Task_abon.presentation.model.Task
import ua.POE.Task_abon.utils.XmlResult
import ua.POE.Task_abon.utils.mapLatestIterable
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val dynamicTaskData: TaskDataDaoImpl,
    private val task: TaskDao,
    private val directory: DirectoryDao,
    private val result: ResultDao,
    private val timing: TimingDao,
    private val xmlRead: XmlRead,
    private val xmlWrite: XmlWrite,
    private val workManager: WorkManager,
) : ViewModel() {

    private val _createXmlState = MutableSharedFlow<String>(0)
    val createXmlState : SharedFlow<String> = _createXmlState

    val tasks: Flow<List<Task>> =
        task.getAll().mapLatestIterable {
            it.toTaskInfo()
        }
            .flowOn(Dispatchers.IO)
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 1)

    fun insert(uri: Uri) = viewModelScope.launch {

        withContext(Dispatchers.IO) {
            when(val result = xmlRead(uri)) {
                is XmlResult.Success -> {
                    _createXmlState.emit(result.message)
                }
                is XmlResult.Fail -> {
                    _createXmlState.emit(result.error)
                }
            }
        }
    }

    fun clearTaskData(taskId: Int) = viewModelScope.launch {
        dynamicTaskData.setUnDone(taskId)
        result.delete(taskId)
        timing.delete(taskId)
    }

    fun deleteTask(taskId: Int) = viewModelScope.launch {
        task.delete(taskId)
        dynamicTaskData.deleteTable(taskId)
        directory.delete(taskId)
        result.delete(taskId)
        timing.delete(taskId)
    }

    fun createXml(taskId: Int, uri: Uri?) {
        uri?.let {
            viewModelScope.launch {
                when (val result = xmlWrite(taskId, uri)) {
                    is XmlResult.Success -> {
                        _createXmlState.emit(result.message)
                        uploadImagesRequestBuilder(taskId)
                    }
                    is XmlResult.Fail -> {
                        _createXmlState.emit(result.error)
                    }
                }
            }
        }
    }

    private fun uploadImagesRequestBuilder(taskId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val photoUris = result.getAllPhotos(taskId)
                workManager.enqueueUniqueWork(
                    UploadWorker.WORK_NAME,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    UploadWorker.makeRequest(photoUris.toTypedArray())
                )
            }
        }
    }
}