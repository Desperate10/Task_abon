package ua.POE.Task_abon.presentation.model


data class BasicInfo(
    val personalAccount: String,
    val address: String,
    val name: String,
    val counter: String,
    val identificationCode: String,
    val counterPlace: String,
    val objectProperties: String,
    val other: String,
    val phoneNumber: String,
    val isPillarChecked: Boolean
)
