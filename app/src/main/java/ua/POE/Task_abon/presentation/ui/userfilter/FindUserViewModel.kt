package ua.POE.Task_abon.presentation.ui.userfilter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.POE.Task_abon.data.dao.DirectoryDao
import ua.POE.Task_abon.data.dao.TaskDataDaoImpl
import ua.POE.Task_abon.presentation.model.Task
import javax.inject.Inject

@HiltViewModel
class FindUserViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val directoryDao: DirectoryDao,
    private val dynamicTaskData: TaskDataDaoImpl
) : ViewModel() {

    private val task = savedStateHandle.get<Task>("task") ?: throw RuntimeException("task is null")
    private val taskId = task.id

    private val _searchFieldsValues = MutableStateFlow<List<String>>(emptyList())
    val searchFieldsValues: StateFlow<List<String>> = _searchFieldsValues

    val filterHashList = MutableStateFlow<List<Map<String,String>>>(emptyList())

    val searchFields: StateFlow<List<String>> = directoryDao.getSearchFields(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun getSearchFieldValues(value: String) {
        viewModelScope.launch {
            val fieldName = getSearchFieldNameByTxt(taskId, value)
            _searchFieldsValues.value =
                dynamicTaskData.getSearchedItemsByField("TD$taskId", fieldName)
        }
    }

    fun updateFilter(filter: List<Map<String,String>>) {
        filterHashList.value = filter
    }

    private suspend fun getSearchFieldNameByTxt(taskId: Int, field: String) =
        directoryDao.getSearchFieldName(taskId, field)


}