package ua.POE.Task_abon.presentation.model

data class DataToSave(
    val date: String,
    val zone1: String,
    val zone2: String,
    val zone3: String,
    val note: String,
    val phoneNumber: String,
    val isMainPhone: Boolean,
    val lat: String,
    val lng: String,
    val photo: String,
    val selectCustomer: Boolean,
    val isNext: Boolean
)
