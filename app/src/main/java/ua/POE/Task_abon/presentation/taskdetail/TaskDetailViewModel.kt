package ua.POE.Task_abon.presentation.taskdetail

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ua.POE.Task_abon.data.entities.UserData
import ua.POE.Task_abon.data.repository.TaskDetailRepository

class TaskDetailViewModel @ViewModelInject constructor( val repository: TaskDetailRepository) : ViewModel() {

    var customersFilterStatus = MutableLiveData(ALL)

    fun getUsersByStatus(table: String, query: String) : List<UserData> {
        return repository.getUserByStatus(table, query)
    }

    fun getFinishedCount(taskId: Int) : LiveData<Int> {
        return repository.getResultsCount(taskId)
    }

    fun getUsers(taskId: Int, params : Map<String, String>?) : List<UserData> {
        val keys = ArrayList<String>()
        val values = ArrayList<String>()

        if (params != null) {
            for ((key, value) in params) {
                val keyName : String = repository.getSearchedFieldName(taskId, key)
                keys.add(keyName)
                values.add(value)
            }
        }

        return repository.getUsers(taskId, keys, values)
    }

    companion object {
        const val ALL = "Всі"
    }

}