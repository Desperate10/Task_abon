package ua.POE.Task_abon.data.mapper

import ua.POE.Task_abon.data.entities.TaskEntity
import ua.POE.Task_abon.presentation.model.Task

fun TaskEntity.toTaskInfo(): Task {
    return Task(
        id = id,
        name = name,
        date = date,
        userCount = count.toInt(),
        filial = filial,
        fileName = fileName ?: "",
        isJur = isJur
    )
}

