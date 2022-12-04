package ua.POE.Task_abon.presentation.model

import android.net.Uri

/**
 * Model for transfer data to from controller input
 * */
data class DataToSave(
    val userIndex: Int,
    val status: Int,
    val sourceCode: String,
    val features: List<String>,
    val date: String,
    val zone1: String,
    val zone2: String,
    val zone3: String,
    val note: String,
    val identificationCode: String,
    val phoneNumber: String,
    val isMainPhone: Boolean,
    val lat: String,
    val lng: String,
    val photoUri: Uri?,
    val selectCustomer: Boolean,
    val isNext: Boolean
)
