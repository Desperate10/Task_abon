package ua.POE.Task_abon.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ua.POE.Task_abon.data.entities.CatalogEntity

@Dao
interface CatalogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(catalog : CatalogEntity)

    @Query("DELETE FROM catalog WHERE type = 1 and type = 0")
    suspend fun deleteTypeOne()

    @Query("SELECT EXISTS (SELECT * FROM catalog WHERE type =:type and code = :code)")
    suspend fun isCatalogItemExists(type: String, code: String) : Boolean

    @Query("SELECT * FROM catalog WHERE type = :type")
    suspend fun getSourceList(type: String) : List<CatalogEntity>

    @Query("SELECT * FROM catalog WHERE type = \"4\"")
    fun getFeatureList() : Flow<List<CatalogEntity>>

    @Query("SELECT text FROM catalog WHERE code = :code AND type = :type")
    suspend fun getSourceByCode(code: String, type: String) : String

    @Query("SELECT text FROM catalog WHERE type = 5")
    suspend fun getOperatorsList() : List<String>

}