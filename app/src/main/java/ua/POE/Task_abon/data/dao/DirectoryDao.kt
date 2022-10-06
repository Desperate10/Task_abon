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
    suspend fun updateBlockInf(taskId: Int, attributeName : String, attributeValue: String)

    @Query("UPDATE directory SET fieldSearch = :attributeValue WHERE taskId = :taskId AND fieldName = :attributeName")
    suspend fun updateBlockSearch(taskId: Int, attributeName: String, attributeValue: String)

    @Query("UPDATE directory SET fieldBlockName = :attributeValue WHERE taskId = :taskId AND fieldBlockInf = :attributeName")
    suspend fun updateBlockName(taskId: Int, attributeName: String, attributeValue: String)

    @Query("UPDATE directory SET levelId = :attributeValue WHERE taskId = :taskId AND fieldName = :attributeName")
    suspend fun updateLevel(taskId: Int, attributeName : String, attributeValue: String)

    @Query("UPDATE directory SET fieldSort = :attributeValue WHERE taskId = :taskId AND fieldName = :attributeName")
    suspend fun updateSort(taskId: Int, attributeName : String, attributeValue: String)

    @Query("SELECT * FROM directory WHERE fieldBlockName= :name and taskId = :taskId")
    fun getFieldsByBlockName(name : String, taskId: Int) : List<Directory>

    @Query("SELECT DISTINCT fieldBlockName FROM directory WHERE fieldBlockName != \"\"")
    fun getFieldNames() : MutableList<String>

    @Query("DELETE FROM directory WHERE taskId = :taskId")
    suspend fun deleteByTaskId(taskId: Int)

    @Query("SELECT fieldSearch FROM directory WHERE taskId=:taskId AND fieldSearch != \"\"")
    fun getSearchFieldsTxt(taskId: Int) : MutableList<String>

    @Query("SELECT fieldName FROM directory WHERE taskId = :taskId AND fieldSearch = :field")
    fun getSearchFieldName(taskId: Int, field :String) : String

    @Query("SELECT fieldName FROM directory WHERE taskId = :taskId AND fieldSearch = :field ")
     fun getSearchFieldNames(taskId: Int, field: String) : String

}