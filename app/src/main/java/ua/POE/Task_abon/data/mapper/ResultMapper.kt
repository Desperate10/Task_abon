package ua.POE.Task_abon.data.mapper

import android.net.Uri
import ua.POE.Task_abon.data.entities.ResultEntity
import ua.POE.Task_abon.presentation.model.DataToSave
import ua.POE.Task_abon.presentation.model.Task
import ua.POE.Task_abon.presentation.model.Technical

object ResultMapper {

    fun mapNeededDataToResult(
        task: Task,
        missingData: Map<String, String>,
        newData: DataToSave,
        technical: Technical
    ): ResultEntity {
        return ResultEntity(
            task.name,
            task.date,
            task.id,
            task.filial,
            newData.userIndex,
            missingData["num"]!!,
            missingData["accountId"]!!,
            newData.date,
            newData.status.toString(),
            newData.sourceCode,
            newData.features.joinToString(),
            newData.zone1,
            newData.zone2,
            newData.zone3,
            newData.note,
            missingData["tel"]!!,
            newData.phoneNumber,
            isMainPhoneToInt(newData.isMainPhone),
            "",
            technical.type,
            technical.counter,
            technical.zoneCount,
            technical.capacity,
            technical.averageUsage,
            newData.lat,
            newData.lng,
            missingData["Numbpers"],
            missingData["family"],
            missingData["Adress"],
            getValidUri(newData.photoUri),
            missingData["counpleas"]
        )
    }

    private fun isMainPhoneToInt(isNew: Boolean): Int {
        return if (isNew) 1 else 0
    }

    private fun getValidUri(uri: Uri?): String? {
        return if (uri.toString().length > NULL) uri.toString() else null
    }

    private const val NULL = 4
}
