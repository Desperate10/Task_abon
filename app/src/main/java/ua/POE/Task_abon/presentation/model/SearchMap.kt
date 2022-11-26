package ua.POE.Task_abon.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchMap(val map : HashMap<String,String>) : Parcelable