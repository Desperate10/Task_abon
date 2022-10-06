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
    fun getUserList(query: SimpleSQLiteQuery) : List<UserData>

    @RawQuery
    fun getSearchedUsersList(query: SimpleSQLiteQuery) : List<UserData>

    @RawQuery
    fun checkField(query: SimpleSQLiteQuery): Int

    @Insert
    fun insertRow(entity: TestEntity) : Long

    @Query("INSERT INTO base (name) VALUES (:name)")
    fun insertColumnName(name : String)

 /*   @Query("UPDATE base SET value = :value WHERE name = :name")
    suspend fun insertColumnValue(tableName: String, name: String, value : String)

    @Query("SELECT count() FROM base")
    fun getRowCount() : Int*/
}