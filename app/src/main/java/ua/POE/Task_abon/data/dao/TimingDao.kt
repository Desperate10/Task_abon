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

    @Query("INSERT INTO timing VALUES (:taskId, :startTaskTime ,null, 0, 0, null)")
    fun addTimingForTask(taskId: String, startTaskTime: String)

    @Query("SELECT * FROM timing WHERE task_id = :taskId LIMIT 1")
    fun getTiming(taskId: String): Timing

    @Query("SELECT startTaskTime FROM timing WHERE task_id = :taskId LIMIT 1")
    fun getStartTaskDate(taskId: String): String

    @Query("SELECT firstEditDate FROM timing WHERE task_id = :taskId LIMIT 1")
    fun getFirstEditDate(taskId: String): String

    @Query("SELECT editSeconds FROM timing WHERE task_id = :taskId LIMIT 1")
    fun getEditTime(taskId: String): Int

    @Query("UPDATE timing SET firstEditDate = :firstEditDate WHERE task_id = :taskId")
    fun updateFirstEditDate(taskId: String, firstEditDate: String)

    @Query("UPDATE timing SET editCount = :editCount WHERE task_id = :taskId")
    fun updateEditCount(taskId: String, editCount: Int)

    @Query("UPDATE timing SET editSeconds = :editSeconds WHERE task_id = :taskId")
    fun updateEditSeconds(taskId: String, editSeconds: Int)

    @Query("UPDATE timing SET lastEditDate = :lastEditDate WHERE task_id = :taskId")
    fun updateLastEditDate(taskId: String, lastEditDate: String)

    @Query("UPDATE timing SET startTaskTime = :startTaskTime WHERE task_id = :taskId")
    fun updateStartTaskDate(taskId: String, startTaskTime: String)

    @Query("UPDATE timing SET editCount = editCount+1 WHERE task_id = :taskId")
    fun upEditCount(taskId: String)

}