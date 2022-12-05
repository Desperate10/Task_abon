package ua.POE.Task_abon.data.mapper

import ua.POE.Task_abon.presentation.model.SavedData
import ua.POE.Task_abon.data.entities.ResultEntity


fun mapResultToSavedData(result: ResultEntity?) = SavedData(
    status = result?.notDone,
    source = result?.dataSource,
    date = result?.doneDate,
    zone1 = result?.zone1,
    zone2 = result?.zone2,
    zone3 = result?.zone3,
    pointCondition = result?.pointCondition,
    note = result?.note,
    identificationCode = result?.identificationCode,
    phoneNumber = result?.phoneNumber,
    isMainPhone = result?.isMain != 0,
    photo = result?.photo
)