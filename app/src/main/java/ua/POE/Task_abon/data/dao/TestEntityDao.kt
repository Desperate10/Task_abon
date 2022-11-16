package ua.POE.Task_abon.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import ua.POE.Task_abon.data.entities.TestEntity
import ua.POE.Task_abon.data.entities.UserData

@Dao
interface TestEntityDao {

    @RawQuery
    suspend fun getSearchedUsersList(query: SimpleSQLiteQuery) : List<UserData>
}