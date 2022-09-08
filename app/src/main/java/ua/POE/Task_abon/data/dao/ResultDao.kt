package ua.POE.Task_abon.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ua.POE.Task_abon.data.entities.Result


@Dao
interface ResultDao {

    @Query("SELECT * FROM result WHERE TSzdn_id = :taskId")
    fun getResultByTaskId(taskId: String) : List<Result>

    @Query("SELECT photo FROM result WHERE TSzdn_id = :taskId AND NULLIF(photo, '') IS NOT NULL ")
    fun getAllPhotos(taskId: String) : List<String>

    @Query("SELECT COUNT(TSzdn_id) FROM result WHERE TSzdn_id = :taskId")
    fun getCount(taskId: String): Flow<Int>

   /* @Query("INSERT INTO result(TSzdn_id, Numb, DT_vpl, No_vpln, Istochnik, Pok_1, Pok_2, pok_3, Note ) VALUES (1, 1, :date, :isDone, :source, :zone1, :zone2, :zone3, :note) ")
    suspend fun insertNewData(date: String, isDone: String, source: String, zone1: String, zone2: String, zone3: String, note: String)
*/
    @Insert(onConflict = REPLACE)
    suspend fun insertNewData(result: Result)

    @Query("UPDATE result SET DT_vpl = :date, No_vpln = :isDone, Istochnik = :source, Pok_1 = :zone1, Pok_2 = :zone2, pok_3 = :zone3, Note = :note WHERE TSzdn_id = :taskId AND Numb = :num ")
    suspend fun updateNewData(taskId: String, num: String, date: String, isDone: String, source: String, zone1: String, zone2: String, zone3: String, note: String)

    @Query("SELECT * from result WHERE TSzdn_id= :taskId AND Id = :index")
    fun getResultUser(taskId: String, index: Int): Result

    @Query("DELETE FROM result WHERE TSzdn_id = :taskId")
    suspend fun delete(taskId: String)

    /*@Query("SELECT startTaskDate from result WHERE TSzdn_id = :taskId")
    fun checkStartTaskDate(taskId: String): String*/
}