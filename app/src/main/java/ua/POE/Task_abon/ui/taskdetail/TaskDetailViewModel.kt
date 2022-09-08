package ua.POE.Task_abon.ui.taskdetail

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.launch
import ua.POE.Task_abon.data.entities.TestEntity
import ua.POE.Task_abon.data.entities.UserData
import ua.POE.Task_abon.data.repository.TaskDetailRepository

class TaskDetailViewModel @ViewModelInject constructor( val repository: TaskDetailRepository) : ViewModel() {

    fun getUsers(table : String) : LiveData<List<UserData>>  {
        return repository.getUsers(table)
    }

    fun getUsersNotFinished(table: String, query: String) : LiveData<List<UserData>> {
        return repository.getNotDoneUser(table, query)
    }

    fun getFinishedCount(taskId: String) : LiveData<Int> {
        return repository.getResultsCount(taskId)
    }

    fun getSearchedUsers(taskId: String, params : HashMap<String, String>) : LiveData<List<UserData>> {
        val keys = ArrayList<String>()
        val values = ArrayList<String>()

        //for(i in 0 .. params.size) {
           // val hashmap = params[i]
            for ((key, value) in params) {
                val keyName : String = repository.getSearchedFieldName(taskId, key)
                keys.add(keyName)
                values.add("$value")
            }
       // }

        return repository.getSearchedUsers(taskId, keys, values)
    }

}