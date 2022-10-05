package ua.POE.Task_abon.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserData(@PrimaryKey(autoGenerate = true) val number : Int,
                    @ColumnInfo(name = "_id")val _id : String,
                    val num : Int,
                    @ColumnInfo(name = "Numbpers")val numbpers : String?,
                    val family : String?,
                    @ColumnInfo(name = "Adress") val address : String?,
                    @ColumnInfo(name = "Counter_numb") val counterNumb : String?,
                    @ColumnInfo(name = "fiderpach") val fider : String?,
                    @ColumnInfo(name = "opora") val opora : String?,
                    @ColumnInfo(name = "icons_account") val icons_account: String?,
                    @ColumnInfo(name = "icons_counter") val icons_counter: String?,
                    @ColumnInfo(name = "IsDone") val done: String?)
