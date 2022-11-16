package ua.POE.Task_abon.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ua.POE.Task_abon.data.entities.TaskEntity

@Dao
interface TaskDao {

    @Query("SELECT * FROM task")
    fun getAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE id = :taskId")
    suspend fun getTask(taskId : Int) : TaskEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Query("DELETE FROM task WHERE id = :taskId")
    suspend fun deleteById(taskId: Int) : Unit

    @Query("DELETE FROM task")
    suspend fun deleteAll()

}