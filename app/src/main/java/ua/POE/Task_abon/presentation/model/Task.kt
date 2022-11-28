package ua.POE.Task_abon.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    val id: Int = -1,
    val name: String = "",
    val date: String= "",
    val userCount: Int = 0,
    val filial: String= "",
    val fileName: String= "",
    val isJur: String? = "0"
): Parcelable
