package ua.POE.Task_abon.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import org.jetbrains.annotations.NotNull

@Entity(tableName = "timing", primaryKeys = ["task_id", "Numb"])
data class Timing(@ColumnInfo(name = "task_id") @NotNull var taskId: Int,
                  @ColumnInfo(name = "Numb") @NotNull var num: Int,
                  @ColumnInfo(name = "startTaskTime") var startTaskTime: String?,
                  @ColumnInfo(name = "endTaskTime") var endTaskTime: String?,
                  @ColumnInfo(name = "firstEditDate") var firstEditDate: String = "",
                  @ColumnInfo(name = "editCount") var editCount: Int = 0,
                  @ColumnInfo(name = "editSeconds") var editSeconds: Int = 0,
                  @ColumnInfo(name = "lastEditDate") var lastEditDate: String = "")