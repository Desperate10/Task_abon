package ua.POE.Task_abon.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import ua.POE.Task_abon.data.entities.UserData



interface UserDataDao {

    @Insert(onConflict = REPLACE)
    suspend fun insert(user: UserData)

    @Insert(onConflict = REPLACE)
    suspend fun insertAll(userId: List<UserData>)

}