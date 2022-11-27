package ua.POE.Task_abon.presentation.ui.taskdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.POE.Task_abon.data.dao.DirectoryDao
import ua.POE.Task_abon.data.dao.ResultDao
import ua.POE.Task_abon.data.dao.impl.TaskCustomerDaoImpl
import ua.POE.Task_abon.data.entities.UserData
import ua.POE.Task_abon.presentation.model.TaskInfo
import ua.POE.Task_abon.presentation.model.SearchMap
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val directoryDao: DirectoryDao,
    private val taskCustomerData: TaskCustomerDaoImpl,
    resultDao: ResultDao
) : ViewModel() {

    private val taskInfo = savedStateHandle.get<TaskInfo>("taskInfo")
    private val taskId = taskInfo?.id ?: throw NullPointerException("taskId in null")
    private val searchParams = savedStateHandle.get<SearchMap>("searchList")

    private val _customerFilterStatus = MutableSharedFlow<String>(2)
    private val customers = MutableStateFlow<List<UserData>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val getCustomersData = _customerFilterStatus
        .flatMapLatest {
            getCustomers(status = it)
            customers
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val finishedCustomersCount = resultDao.getResultCount(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    private suspend fun getCustomers(status: String?) {
        val keys = ArrayList<String>()
        val values = ArrayList<String>()

        searchParams?.let {
            for ((key, value) in searchParams.map) {
                val keyName: String = directoryDao.getSearchFieldName(taskId, key)
                keys.add(keyName)
                values.add(value)
            }
        }
        customers.value = taskCustomerData.getUsers(taskId, keys, values, status)
    }

    fun resetFilter() {
        viewModelScope.launch {
            searchParams?.map?.clear()
            _customerFilterStatus.emit(ALL)
        }
    }

    fun setCustomerStatus(isChecked: Boolean) {
        viewModelScope.launch {
            if (isChecked) _customerFilterStatus.emit(NOT_FINISHED)
            else _customerFilterStatus.emit(ALL)
        }
    }

    companion object {
        const val NOT_FINISHED = "Не виконано"
        const val ALL = "Всі"
    }

}