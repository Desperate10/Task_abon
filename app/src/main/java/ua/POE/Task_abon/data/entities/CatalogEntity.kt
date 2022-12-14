package ua.POE.Task_abon.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity that holding data to populate spinners in UserInfoFragment
 **/
@Entity(tableName = "catalog")
data class CatalogEntity(@PrimaryKey(autoGenerate = true) var id: Int,
                         var type: String?,
                         var code: String?,
                         var text : String?)
