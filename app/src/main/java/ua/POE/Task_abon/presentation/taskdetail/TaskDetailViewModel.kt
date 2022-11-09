package ua.POE.Task_abon.presentation.taskdetail

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
    private val searchParams = savedStateHandle.get("searchList") as Map<String, String>?

    //заменить LiveData на Flow
    var customersFilterStatus = MutableLiveData(ALL)

    private val _customerFilterStatus = MutableStateFlow<String>(ALL)
    //private val finishedCustomersCount = MutableStateFlow(0)

    //private val _users = MutableStateFlow<List<UserData>>(emptyList())

    /*fun getUsersByStatus(table: String, query: String): List<UserData> {
        return repository.getUserByStatus(table, query)
    }*/

    val users = _customerFilterStatus
        .flatMapLatest {
            getUsers(status = it)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val finishedCustomersCount = repository.getResultsCount(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)
    /*fun getFinishedCount() {
        viewModelScope.launch {
            finishedCustomersCount.value = repository.getResultsCount(taskId)
        }
    }*/

    private fun getUsers(status: String?) = flow {
        val keys = ArrayList<String>()
        val values = ArrayList<String>()

        if (searchParams != null) {
            for ((key, value) in searchParams) {
                val keyName: String = repository.getSearchedFieldName(taskId, key)
                keys.add(keyName)
                values.add(value)
            }
        }
        emit(repository.getUsers(taskId, keys, values, status))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    /*fun getUsers(taskId: Int, params: Map<String, String>?) {
        viewModelScope.launch {
            val keys = ArrayList<String>()
            val values = ArrayList<String>()

            if (params != null) {
                for ((key, value) in params) {
                    val keyName: String = repository.getSearchedFieldName(taskId, key)
                    keys.add(keyName)
                    values.add(value)
                }
            }
            _users.value = repository.getUsers(taskId, keys, values)
        }
    }*/
    fun setCustomerStatus(isChecked: Boolean) {
        if (isChecked) _customerFilterStatus.value = NOT_FINISHED
                else _customerFilterStatus.value = ALL
    }

    companion object {
        const val NOT_FINISHED = "Не виконано"
        const val ALL = "Всі"
    }

}