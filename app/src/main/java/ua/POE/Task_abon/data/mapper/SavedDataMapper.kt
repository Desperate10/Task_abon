package ua.POE.Task_abon.data.mapper

import ua.POE.Task_abon.presentation.model.SavedData
import ua.POE.Task_abon.data.entities.Result


fun mapResultToSavedData(result: Result?) = SavedData(
    status = result?.notDone,
    source = result?.dataSource,
    date = result?.doneDate,
    zone1 = result?.zone1,
    zone2 = result?.zone2,
    zone3 = result?.zone3,
    pointCondition = result?.point_condition,
    note = result?.note,
    phoneNumber = result?.phoneNumber,
    isMainPhone = result?.is_main != 0,
    photo = result?.photo
)