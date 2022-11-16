package ua.POE.Task_abon.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ua.POE.Task_abon.data.entities.Result


@Dao
interface ResultDao {

    @Query("SELECT * FROM result WHERE TSzdn_id = :taskId")
    suspend fun getResultByTaskId(taskId: Int) : List<Result>

    @Query("SELECT photo FROM result WHERE TSzdn_id = :taskId AND NULLIF(photo, '') IS NOT NULL ")
    suspend fun getAllPhotos(taskId: Int) : List<String>

    @Query("SELECT COUNT(TSzdn_id) FROM result WHERE TSzdn_id = :taskId")
    fun getResultCount(taskId: Int): Flow<Int>

    @Insert(onConflict = REPLACE)
    suspend fun insertNewData(result: Result)

    @Query("UPDATE result SET DT_vpl = :date, No_vpln = :isDone, Istochnik = :source, Pok_1 = :zone1, Pok_2 = :zone2, pok_3 = :zone3, Note = :note WHERE TSzdn_id = :taskId AND Numb = :num ")
    suspend fun updateNewData(taskId: Int, num: String, date: String, isDone: String, source: String, zone1: String, zone2: String, zone3: String, note: String)

    @Query("SELECT * from result WHERE TSzdn_id= :taskId AND Id = :index")
    suspend fun getResultUser(taskId: Int, index: Int): Result?

    @Query("DELETE FROM result WHERE TSzdn_id = :taskId")
    suspend fun delete(taskId: Int)
}