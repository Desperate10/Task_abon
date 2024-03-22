package ua.POE.Task_abon.data.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Entity for storing saved results and creating output xml file
 * */
@Entity(tableName = "result", primaryKeys = ["TSzdn_id", "Numb"])
data class ResultEntity(
    @ColumnInfo(name = "Task_name") var taskName: String?,
    @ColumnInfo(name = "Dt_Crt") var createDate: String?,
    @ColumnInfo(name = "TSzdn_id") var taskId: Int,
    @ColumnInfo(name = "Filial") var filial: String?,
    @ColumnInfo(name = "Id") var index: Int?,
    @ColumnInfo(name = "Numb") var num: String,
    @ColumnInfo(name = "AccountID") var accountId: String?,
    @ColumnInfo(name = "DT_vpl") var doneDate: String?,
    @ColumnInfo(name = "No_vpln") var notDone: String?,
    @ColumnInfo(name = "Istochnik") var dataSource: String?,
    @ColumnInfo(name = "point_condition") var pointCondition: String?,
    @ColumnInfo(name = "Pok_1") var zone1: String?,
    @ColumnInfo(name = "Pok_2") var zone2: String?,
    @ColumnInfo(name = "pok_3") var zone3: String?,
    @ColumnInfo(name = "Note") var note: String?,
    @ColumnInfo(name = "old_tel") var oldPhoneNumber: String?,
    @ColumnInfo(name = "tel") var phoneNumber: String?,
    @ColumnInfo(name = "is_main") var isMain: Int?,
    @ColumnInfo(name = "DT_ins") var insertDate: String?,
    @ColumnInfo(name = "type") var type: String?,
    @ColumnInfo(name = "Counter_numb", defaultValue = "0") var counter: String?,
    @ColumnInfo(name = "Zonnost", defaultValue = "0") var zoneCount: String?,
    @ColumnInfo(name = "Counter_capacity", defaultValue = "0") var counterCapacity: String?,
    @ColumnInfo(name = "Sred_rashod") var avgUsage: String?,
    @ColumnInfo(name = "lat") var lat: String?,
    @ColumnInfo(name = "lng") var lng: String?,
    @ColumnInfo(name = "numbpers") var numbpers: String?,
    @ColumnInfo(name = "family") var family: String?,
    @ColumnInfo(name = "adress") var adress: String?,
    @ColumnInfo(name = "photo") var photo: String?,
    @ColumnInfo(name = "counpleas") var counterPlace: String?,
    @ColumnInfo(name = "Ident_code") var identificationCode: String?,
    @ColumnInfo(name = "Physical_PersonId") var physicalPersonId: String?,
    @ColumnInfo(name = "opr") var opr: String?,
    @ColumnInfo(name = "opr_note") var oprNote: String?
)