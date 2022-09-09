package ua.POE.Task_abon.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import ua.POE.Task_abon.data.entities.Timing

@Dao
interface TimingDao {

    @Insert(onConflict = IGNORE)
    suspend fun insertTiming(timing: Timing)

    @Query("INSERT INTO timing VALUES (:taskId, :num, :startTaskTime ,null, 0, 0, null)")
    fun addTimingForPerson(taskId: String, num: String, startTaskTime: String)

    @Query("SELECT * FROM timing WHERE task_id = :taskId")
    fun getTiming(taskId: String): List<Timing>

    @Query("SELECT startTaskTime FROM timing WHERE task_id = :taskId AND Numb = :num LIMIT 1")
    fun getStartTaskDate(taskId: String, num: String): String

    @Query("SELECT firstEditDate FROM timing WHERE task_id = :taskId AND Numb = :num LIMIT 1")
    fun getFirstEditDate(taskId: String, num: String): String

    @Query("SELECT editSeconds FROM timing WHERE task_id = :taskId AND Numb = :num LIMIT 1")
    fun getEditTime(taskId: String, num: String): Int

    @Query("UPDATE timing SET firstEditDate = :firstEditDate WHERE task_id = :taskId AND Numb = :num")
    fun updateFirstEditDate(taskId: String, num: String, firstEditDate: String)

    @Query("UPDATE timing SET editCount = :editCount WHERE task_id = :taskId AND Numb = :num")
    fun updateEditCount(taskId: String, num: String, editCount: Int)

    @Query("UPDATE timing SET editSeconds = :editSeconds WHERE task_id = :taskId AND Numb = :num")
    fun updateEditSeconds(taskId: String, num: String, editSeconds: Int)

    @Query("UPDATE timing SET lastEditDate = :lastEditDate WHERE task_id = :taskId AND Numb = :num")
    fun updateLastEditDate(taskId: String, num: String, lastEditDate: String)

    @Query("UPDATE timing SET startTaskTime = :startTaskTime WHERE task_id = :taskId AND Numb = :num")
    fun updateStartTaskDate(taskId: String, num: String, startTaskTime: String)

    @Query("UPDATE timing SET editCount = editCount+1 WHERE task_id = :taskId AND Numb = :num")
    fun upEditCount(taskId: String, num: String)

}