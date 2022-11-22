package ua.POE.Task_abon.data.mapper

import ua.POE.Task_abon.data.entities.TaskEntity
import ua.POE.Task_abon.domain.model.TaskInfo

fun TaskEntity.toTaskInfo(): TaskInfo {
    return TaskInfo(
        id = id,
        name = name,
        date = date,
        count = count,
        filial = filial,
        fileName = fileName,
        isJur = isJur
    )
}

fun TaskInfo.toTask(): TaskEntity {
    return TaskEntity(
        id = id,
        name = name,
        date = date,
        count = count,
        filial = filial,
        fileName = fileName,
        tableName = "TD$id",
        isJur = isJur
    )
}
