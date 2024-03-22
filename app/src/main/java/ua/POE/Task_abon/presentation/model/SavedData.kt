package ua.POE.Task_abon.presentation.model

/**
 * Model to transfer saved data from controllers input
 * */
data class SavedData(
    val status: String?,
    val source: String?,
    val date: String?,
    val zone1: String?,
    val zone2: String?,
    val zone3: String?,
    val pointCondition: String?,
    val note: String?,
    val identificationCode: String?,
    val phoneNumber: String?,
    val isMainPhone: Boolean?,
    val photo: String?,
    val opr: String?,
    val oprNote: String?
)