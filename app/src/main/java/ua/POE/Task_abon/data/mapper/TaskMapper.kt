package ua.POE.Task_abon.data.mapper

import ua.POE.Task_abon.data.entities.TaskEntity
import ua.POE.Task_abon.presentation.model.TaskInfo

fun TaskEntity.toTaskInfo(): TaskInfo {
    return TaskInfo(
        id = id,
        name = name,
        date = date,
        count = count.toInt(),
        filial = filial,
        fileName = fileName ?: "",
        isJur = isJur
    )
}

