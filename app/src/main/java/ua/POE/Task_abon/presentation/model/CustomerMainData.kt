package ua.POE.Task_abon.presentation.model

import androidx.room.ColumnInfo

/**
 *Model to hold main user data showing in TaskDetailFragment
 * */
data class CustomerMainData(val number : Int,
                            @ColumnInfo(name = "_id")val id : Int,
                            val num : Int,
                            @ColumnInfo(name = "Numbpers")val numbpers : String?,
                            val family : String?,
                            @ColumnInfo(name = "Adress") val address : String?,
                            @ColumnInfo(name = "Counter_numb") val counterNumb : String?,
                            @ColumnInfo(name = "fiderpach") val fider : String?,
                            @ColumnInfo(name = "opora") val opora : String?,
                            @ColumnInfo(name = "icons_account") val iconsAccount: String?,
                            @ColumnInfo(name = "icons_counter") val iconsCounter: String?,
                            @ColumnInfo(name = "IsDone") val done: String?)
