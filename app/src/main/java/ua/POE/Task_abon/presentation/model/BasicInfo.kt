package ua.POE.Task_abon.presentation.model

import org.apache.commons.lang3.StringEscapeUtils

data class BasicInfo(
    val personalAccount: String,
    val address: String,
    val name: String,
    val counter: String,
    val identificationCode: String,
    val other: String,
    val phoneNumber: String
)
