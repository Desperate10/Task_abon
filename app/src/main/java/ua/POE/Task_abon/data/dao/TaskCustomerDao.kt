package ua.POE.Task_abon.data.dao

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import ua.POE.Task_abon.data.entities.UserData

@Dao
interface TaskCustomerDao {

    @RawQuery
    suspend fun getSearchedUsersList(query: SimpleSQLiteQuery) : List<UserData>
}