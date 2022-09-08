package ua.POE.Task_abon.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import ua.POE.Task_abon.data.entities.Directory
import java.util.concurrent.Flow

@Dao
interface DirectoryDao {

    //TODO: getFieldsBy functions

    @Insert(onConflict = REPLACE)
    suspend fun insert(directory : Directory)

    @Query("UPDATE directory SET fieldBlockInf = :attributeValue WHERE taskId = :taskId AND fieldName = :attributeName")
    suspend fun updateBlockInf(taskId : String, attributeName : String, attributeValue: String)

    @Query("UPDATE directory SET fieldSearch = :attributeValue WHERE taskId = :taskId AND fieldName = :attributeName")
    suspend fun updateBlockSearch(taskId: String, attributeName: String, attributeValue: String)

    @Query("UPDATE directory SET fieldBlockName = :attributeValue WHERE taskId = :taskId AND fieldBlockInf = :attributeName")
    suspend fun updateBlockName(taskId: String, attributeName: String, attributeValue: String)

    @Query("UPDATE directory SET levelId = :attributeValue WHERE taskId = :taskId AND fieldName = :attributeName")
    suspend fun updateLevel(taskId : String, attributeName : String, attributeValue: String)

    @Query("UPDATE directory SET fieldSort = :attributeValue WHERE taskId = :taskId AND fieldName = :attributeName")
    suspend fun updateSort(taskId : String, attributeName : String, attributeValue: String)

    @Query("SELECT * FROM directory WHERE fieldBlockName= :name and taskId = :taskId")
    fun getFieldsByBlockName(name : String, taskId: String) : List<Directory>

    @Query("SELECT DISTINCT fieldBlockName FROM directory WHERE fieldBlockName != \"\"")
    fun getFieldNames() : MutableList<String>

    @Query("DELETE FROM directory WHERE taskId = :taskId")
    suspend fun deleteByTaskId(taskId: String)

    @Query("SELECT fieldSearch FROM directory WHERE taskId=:taskId AND fieldSearch != \"\"")
    fun getSearchFieldsTxt(taskId: String) : MutableList<String>

    @Query("SELECT fieldName FROM directory WHERE taskId = :taskId AND fieldSearch = :field")
    fun getSearchFieldName(taskId: String, field :String) : String

    @Query("SELECT fieldName FROM directory WHERE taskId = :taskId AND fieldSearch = :field ")
    fun getSearchFieldNames(taskId: String, field: String) : String

}