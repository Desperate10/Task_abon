package ua.POE.Task_abon.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "timing", primaryKeys = ["task_id", "Numb"])
data class Timing(@ColumnInfo(name = "task_id") var taskId: String,
                  @ColumnInfo(name = "Numb") var num: String,
                  @ColumnInfo(name = "startTaskTime") var startTaskTime: String?,
                  @ColumnInfo(name = "firstEditDate") var firstEditDate: String?,
                  @ColumnInfo(name = "editCount", defaultValue = 0.toString()) var editCount: Int?,
                  @ColumnInfo(name = "editSeconds", defaultValue = 0.toString()) var editSeconds: Int?,
                  @ColumnInfo(name = "lastEditDate") var lastEditDate: String?)
