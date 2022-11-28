package ua.POE.Task_abon.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "directory")
data class DirectoryEntity(@PrimaryKey(autoGenerate = true) var id: Int,
                           var taskId: Int,
                           var fieldName: String?,
                           var fieldNameTxt : String?,
                           var fieldBlockInf : String? = "0",
                           var fieldSort: String? = "999",
                           var fieldSearch : String? = "",
                           var fieldBlockName : String? = "",
                           var levelId : String? = "0")