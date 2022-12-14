package ua.POE.Task_abon.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ua.POE.Task_abon.data.entities.DirectoryEntity

@Dao
interface DirectoryDao {

    @Insert(onConflict = REPLACE)
    suspend fun insert(directory: DirectoryEntity)

    @Query("UPDATE directory SET fieldBlockInf = :attributeValue WHERE taskId = :taskId AND fieldName = :attributeName")
    suspend fun updateBlockInfo(taskId: Int, attributeName: String, attributeValue: String)

    @Query("UPDATE directory SET fieldSearch = :attributeValue WHERE taskId = :taskId AND fieldName = :attributeName")
    suspend fun updateBlockSearch(taskId: Int, attributeName: String, attributeValue: String)

    @Query("UPDATE directory SET fieldBlockName = :attributeValue WHERE taskId = :taskId AND fieldBlockInf = :attributeName")
    suspend fun updateBlockName(taskId: Int, attributeName: String, attributeValue: String)

    @Query("UPDATE directory SET levelId = :attributeValue WHERE taskId = :taskId AND fieldName = :attributeName")
    suspend fun updateLevel(taskId: Int, attributeName: String, attributeValue: String)

    @Query("UPDATE directory SET fieldSort = :attributeValue WHERE taskId = :taskId AND fieldName = :attributeName")
    suspend fun updateSort(taskId: Int, attributeName: String, attributeValue: String)

    @Query("SELECT fieldName FROM directory WHERE fieldBlockName= :name and taskId = :taskId")
    suspend fun getFieldsByBlockName(name: String, taskId: Int): List<String>

    @Query("SELECT fieldName FROM directory WHERE fieldBlockName= '' and taskId = :taskId")
    suspend fun getBasicFields(taskId: Int): List<String>

    @Query("SELECT DISTINCT fieldBlockName FROM directory WHERE fieldBlockName != \"\"")
    suspend fun getBlockNames(): List<String>

    @Query("DELETE FROM directory WHERE taskId = :taskId")
    suspend fun delete(taskId: Int)

    @Query("SELECT fieldSearch FROM directory WHERE taskId=:taskId AND fieldSearch != \"\"")
    fun getSearchFields(taskId: Int): Flow<List<String>>

    @Query("SELECT fieldName FROM directory WHERE taskId = :taskId AND fieldSearch = :field")
    suspend fun getSearchFieldName(taskId: Int, field: String): String

}