package ua.POE.Task_abon.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.flow.Flow
import ua.POE.Task_abon.data.dao.DirectoryDao
import ua.POE.Task_abon.data.dao.ResultDao
import ua.POE.Task_abon.data.dao.TestEntityDao
import ua.POE.Task_abon.data.entities.UserData
import java.lang.StringBuilder
import javax.inject.Inject

class TaskDetailRepository @Inject constructor(private val testEntityDao: TestEntityDao,private val directoryDao: DirectoryDao, private val resultDao: ResultDao) {

    /*fun getUserByStatus(table: String, query: String) : List<UserData> {
        return testEntityDao.getUserList(SimpleSQLiteQuery("SELECT * FROM $table WHERE IsDone = \"$query\""))
    }*/

    suspend fun getSearchedFieldName(taskId: Int, key:String) : String {
        return directoryDao.getSearchFieldName(taskId, key)
    }

    suspend fun getUsers(taskId: Int, keys: List<String>, values:ArrayList<String>, status: String?) : List<UserData> {
        val whereSize = keys.size
        var whereClause = StringBuilder()
        var query = "SELECT * FROM TD$taskId "

        for(i in 0 until whereSize) {
            if(i ==0) query += "WHERE "
            whereClause = if (i != whereSize-1) {
                whereClause.append("${keys[i]} LIKE \"%${values[i]}%\" AND ")
            } else {
                whereClause.append("${keys[i]} LIKE \"%${values[i]}%\"")
            }
        }
        if (status.equals("Не виконано")) {
            if (whereSize>0) {
                whereClause.append(" AND IsDone = \"${status}\"")
            } else {
                whereClause.append(" WHERE IsDone = \"${status}\"")
            }
        }
        val st = query + whereClause.toString()

        return testEntityDao.getSearchedUsersList(SimpleSQLiteQuery(st))
    }

    /*suspend fun getUsers(taskId: Int, keys: List<String>, values:ArrayList<String>) : List<UserData> {
        val whereSize = keys.size
        var whereClause = StringBuilder()
        var query = "SELECT * FROM TD$taskId "

        for(i in 0 until whereSize) {
            if(i ==0) query += "WHERE "
            whereClause = if (i != whereSize-1) {
                whereClause.append("${keys[i]} LIKE \"%${values[i]}%\" AND ")
            } else {
                whereClause.append("${keys[i]} LIKE \"%${values[i]}%\"")
            }
        }
        var st = query + whereClause.toString()

        return testEntityDao.getSearchedUsersList(SimpleSQLiteQuery("$st"))
    }*/

    fun getResultsCount(taskId : Int) : Flow<Int> {
        return resultDao.getCount(taskId)
    }
}