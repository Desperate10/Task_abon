package ua.POE.Task_abon.data.mapper

import ua.POE.Task_abon.data.entities.TaskEntity
import ua.POE.Task_abon.domain.model.TaskInfo

fun TaskEntity.toTaskInfo(): TaskInfo {
    return TaskInfo(
        name = name,
        date = date,
        count = count,
        fileName = fileName,
        isJur = isJur
    )
}

fun TaskInfo.toTask(): TaskEntity {
    return TaskEntity(
        name = name,
        date = date,
        count = count,
        filial = "",
        fileName = fileName,
        tableName = "TD$fileName",
        isJur = isJur
    )
}