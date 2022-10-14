package ua.POE.Task_abon.data.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.sqlite.db.SupportSQLiteDatabase

@Entity(tableName = "task")
data class TaskEntity(@PrimaryKey val id : Int = 0,
                      val name : String,
                      val date : String,
                      val count : String,
                      val filial : String,
                      val fileName: String?,
                      val tableName: String,
                      val isJur : String? = "0")