package ua.POE.Task_abon.domain.model

data class SavedData(
    val status: String?,
    val source: String?,
    val date: String?,
    val zone1: String?,
    val zone2: String?,
    val zone3: String?,
    val pointCondition: String?,
    val note: String?,
    val phoneNumber: String?,
    val isMainPhone: Boolean?,
    val photo: String?
)