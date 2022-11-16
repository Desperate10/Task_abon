package ua.POE.Task_abon.presentation.taskdetail

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.POE.Task_abon.data.entities.UserData
import ua.POE.Task_abon.data.repository.TaskDetailRepository
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: TaskDetailRepository
) : ViewModel() {

    private val taskId = savedStateHandle.get<Int>("taskId") ?: throw NullPointerException("TaskId is null")
    private val searchParams = savedStateHandle.get("searchList") as MutableMap<String, String>?

    private val _customerFilterStatus = MutableSharedFlow<String>(2)
    private val customers = MutableStateFlow<List<UserData>>(emptyList())

    val getCustomersData = _customerFilterStatus
        .flatMapLatest {
            getCustomers(status = it)
            customers
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList<UserData>())

    val finishedCustomersCount = repository.getResultsCount(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    private fun getCustomers(status: String?) {
        viewModelScope.launch {
            val keys = ArrayList<String>()
            val values = ArrayList<String>()

            searchParams?.let {
                for ((key, value) in searchParams) {
                    val keyName: String = repository.getSearchedFieldName(taskId, key)
                    keys.add(keyName)
                    values.add(value)
                }
            }
            customers.value = repository.getUsers(taskId, keys, values, status)
        }
    }

    fun resetFilter() {
        viewModelScope.launch {
            searchParams?.clear()
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