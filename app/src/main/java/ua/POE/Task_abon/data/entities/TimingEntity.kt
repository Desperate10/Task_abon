package ua.POE.Task_abon.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Entity for storing timings that controller spends during the tour
 * */
@Entity(tableName = "timing", primaryKeys = ["task_id", "Numb"])
data class TimingEntity(@ColumnInfo(name = "task_id") var taskId: Int,
                        @ColumnInfo(name = "Numb") var num: Int,
                        @ColumnInfo(name = "startTaskTime") var startTaskTime: String?,
                        @ColumnInfo(name = "endTaskTime") var endTaskTime: String?,
                        @ColumnInfo(name = "firstEditDate") var firstEditDate: String = "",
                        @ColumnInfo(name = "editCount") var editCount: Int = 0,
                        @ColumnInfo(name = "editSeconds") var editSeconds: Int = 0,
                        @ColumnInfo(name = "lastEditDate") var lastEditDate: String = "")