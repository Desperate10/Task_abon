package ua.POE.Task_abon.data.repository

import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import ua.POE.Task_abon.data.AppDatabase
import ua.POE.Task_abon.data.dao.DirectoryDao
import ua.POE.Task_abon.data.dao.ResultDao
import ua.POE.Task_abon.data.dao.TestEntityDao
import ua.POE.Task_abon.data.entities.TestEntity
import ua.POE.Task_abon.data.entities.UserData
import java.lang.Exception
import java.lang.StringBuilder
import javax.inject.Inject

class TaskDetailRepository @Inject constructor(private val testEntityDao: TestEntityDao,private val directoryDao: DirectoryDao, private val resultDao: ResultDao) {

    fun getUsers(table: String) : LiveData<List<UserData>> {
        return testEntityDao.getUserList(SimpleSQLiteQuery("SELECT * FROM $table"))
                    .asLiveData()
    }

    fun getNotDoneUser(table: String, query: String) : LiveData<List<UserData>> {
        return testEntityDao.getUserList(SimpleSQLiteQuery("SELECT * FROM $table WHERE IsDone <> \"$query\"")).asLiveData()
    }

    fun getSearchedFieldName(taskId: String, key:String) : String {
        return directoryDao.getSearchFieldNames(taskId, key)
    }

    fun getSearchedUsers(taskId: String, keys: List<String>, values:ArrayList<String>) : LiveData<List<UserData>> {
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

        return testEntityDao.getSearchedUsersList(SimpleSQLiteQuery("$st")).asLiveData()
    }

    fun getResultsCount(taskId : String) : LiveData<Int> {
        return resultDao.getCount(taskId).asLiveData()
    }
}