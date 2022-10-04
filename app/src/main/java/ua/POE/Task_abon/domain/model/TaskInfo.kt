package ua.POE.Task_abon.domain.model

import androidx.room.PrimaryKey

data class TaskInfo(
    val id: Int,
    val name: String,
    val date: String,
    val count: String,
    val fileName: String?,
    val isJur: String? = "0"
)
