package ua.POE.Task_abon.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity that holding info about uploaded tasks
 * */
@Entity(tableName = "task")
data class TaskEntity(@PrimaryKey val id : Int = 0,
                      val name : String,
                      val date : String,
                      val count : String,
                      val filial : String,
                      val fileName: String?,
                      val tableName: String,
                      val isJur : String? = "0")