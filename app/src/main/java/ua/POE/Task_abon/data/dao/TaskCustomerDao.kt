package ua.POE.Task_abon.data.dao

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import ua.POE.Task_abon.data.entities.UserDataEntity

@Dao
interface TaskCustomerDao {

    @RawQuery
    suspend fun getSearchedCustomersList(query: SimpleSQLiteQuery) : List<UserDataEntity>
}