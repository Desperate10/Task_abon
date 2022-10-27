package ua.POE.Task_abon.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import ua.POE.Task_abon.data.entities.Timing

@Dao
interface TimingDao {

    @Insert(onConflict = IGNORE)
    suspend fun insertTiming(timing: Timing)

    @Query("INSERT INTO timing VALUES (:taskId, :num, :startTaskTime, null ,null, 0, 0, null)")
    fun addTimingForPerson(taskId: Int, num: Int, startTaskTime: String)

    @Query("SELECT * FROM timing WHERE task_id = :taskId")
    fun getTiming(taskId: Int): List<Timing>

    @Query("SELECT startTaskTime FROM timing WHERE task_id = :taskId AND Numb = :num LIMIT 1")
     fun getStartTaskDate(taskId: Int, num: Int): String

    @Query("SELECT firstEditDate FROM timing WHERE task_id = :taskId AND Numb = :num LIMIT 1")
    fun getFirstEditDate(taskId: Int, num: Int): String

    @Query("SELECT editSeconds FROM timing WHERE task_id = :taskId AND Numb = :num LIMIT 1")
    fun getEditTime(taskId: Int, num: Int): Int

    @Query("UPDATE timing SET firstEditDate = :firstEditDate WHERE task_id = :taskId AND Numb = :num")
    fun updateFirstEditDate(taskId: Int, num: Int, firstEditDate: String)

    @Query("UPDATE timing SET editCount = :editCount WHERE task_id = :taskId AND Numb = :num")
    fun updateEditCount(taskId: Int, num: Int, editCount: Int)

    @Query("UPDATE timing SET editSeconds = :editSeconds WHERE task_id = :taskId AND Numb = :num")
    fun updateEditSeconds(taskId: Int, num: Int, editSeconds: Int)

    @Query("UPDATE timing SET lastEditDate = :lastEditDate WHERE task_id = :taskId AND Numb = :num")
    fun updateLastEditDate(taskId: Int, num: Int, lastEditDate: String)

    @Query("UPDATE timing SET startTaskTime = :startTaskTime WHERE task_id = :taskId AND Numb = :num")
    fun updateStartTaskDate(taskId: Int, num: Int, startTaskTime: String)

    @Query("UPDATE timing SET editCount = editCount+1 WHERE task_id = :taskId AND Numb = :num")
    fun upEditCount(taskId: Int, num: Int)

    @Query("DELETE FROM timing WHERE task_id = :taskId")
    suspend fun deleteByTaskId(taskId: Int)

}