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
            taskName = task.name,
            createDate = task.date,
            taskId = task.id,
            filial = task.filial,
            index = newData.userIndex,
            num = missingData["num"]!!,
            accountId = missingData["accountId"]!!,
            doneDate = newData.date,
            notDone = newData.status.toString(),
            dataSource = newData.sourceCode,
            pointCondition = newData.features.joinToString(),
            zone1 = newData.zone1,
            zone2 = newData.zone2,
            zone3 = newData.zone3,
            note = newData.note.replace("[\\t\\n\\r]+", " "),
            oldPhoneNumber = missingData["tel"]!!,
            phoneNumber = newData.phoneNumber,
            isMain = isMainPhoneToInt(newData.isMainPhone),
            insertDate = "",
            type = technical.type,
            counter = technical.counter,
            zoneCount = technical.zoneCount,
            counterCapacity = technical.capacity,
            avgUsage = technical.averageUsage,
            lat = newData.lat,
            lng = newData.lng,
            numbpers = missingData["Numbpers"],
            family = missingData["family"],
            adress = missingData["Adress"],
            photo = getValidUri(newData.photoUri),
            counterPlace = missingData["counpleas"],
            identificationCode = newData.identificationCode,
            physicalPersonId = missingData["Physical_PersonId"],
            pillarChecked = newData.pillarCheckedStatus,
            newPillar = newData.newPillar,
            newPillarDescription = newData.newPillarNote,
            pillarLat = newData.newPillarLat,
            pillarLng = newData.newPillarLng
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
