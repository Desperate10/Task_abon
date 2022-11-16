package ua.POE.Task_abon.domain.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import ua.POE.Task_abon.domain.model.TaskInfo
import ua.POE.Task_abon.utils.Resource

interface TaskRepository {

    fun getTaskInfoList(): LiveData<List<TaskInfo>>

    fun getTaskInfo(taskId: String) : LiveData<TaskInfo>

    suspend fun loadTaskFromXml(uri: Uri): Resource<String>

}