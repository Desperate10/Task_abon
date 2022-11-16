package ua.POE.Task_abon.presentation.userfilter

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
import ua.POE.Task_abon.data.dao.impl.TaskCustomerDaoImpl
import javax.inject.Inject

@HiltViewModel
class FindUserViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val directoryDao: DirectoryDao,
    private val dynamicTaskData: TaskCustomerDaoImpl
) : ViewModel() {

    private val taskId =
        savedStateHandle.get<Int>("taskId") ?: throw RuntimeException("taskId is null")

    private val _searchFieldsValues = MutableStateFlow<List<String>>(emptyList())
    val searchFieldsValues: StateFlow<List<String>> = _searchFieldsValues

    val searchFields: StateFlow<List<String>> = directoryDao.getSearchFields(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun getSearchFieldValues(value: String) {
        viewModelScope.launch {
            val fieldName = getSearchFieldNameByTxt(taskId, value)
            _searchFieldsValues.value =
                dynamicTaskData.getSearchedItemsByField("TD$taskId", fieldName)
        }
    }

    private suspend fun getSearchFieldNameByTxt(taskId: Int, field: String) =
        directoryDao.getSearchFieldName(taskId, field)


}