package ua.POE.Task_abon.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ua.POE.Task_abon.data.AppDatabase
import ua.POE.Task_abon.data.dao.*
import ua.POE.Task_abon.data.entities.*
import ua.POE.Task_abon.network.MyApi
import ua.POE.Task_abon.network.UploadRequestBody
import ua.POE.Task_abon.network.UploadResponse
import java.io.*
import javax.inject.Inject


class  XmlLoader @Inject constructor(val context: Context, val database: AppDatabase, private val taskDao: TaskDao, private val directoryDao: DirectoryDao, private val catalogDao: CatalogDao, private val resultDao: ResultDao) {

    suspend fun readXml(uri: Uri) : String {

        val fileName = getNameFromUri(uri)
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser: XmlPullParser = factory.newPullParser()
        parser.setInput( //StringReader(fileContent)
                BufferedReader(
                        InputStreamReader(
                                context.contentResolver.openInputStream(uri),
                                "windows-1251"
                        )
                )
        )
        var eventType = parser.eventType
        var taskId: String = ""
        var tableName: String = ""
        var sdb: SupportSQLiteDatabase = database.openHelper.writableDatabase
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            println(tagName)
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (tagName) {
                        "Task_st" -> {
                            val name = parser.getAttributeValue(0)
                            val date = parser.getAttributeValue(1)
                            taskId = parser.getAttributeValue(2)
                            tableName = "TD$taskId"
                            if (isTableExists(tableName)) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Вже існує обхідний лист з цим ідентифікатором!", Toast.LENGTH_SHORT).show()
                                }
                                break
                            }
                            // addTable(tableName)
                            val count = parser.getAttributeValue(5)
                            var filial = ""
                            var isJur = "0"
                            if (parser.attributeCount == 8) filial = parser.getAttributeValue(7)
                            if (parser.attributeCount == 9) isJur = parser.getAttributeValue(8)
                            taskDao.insert(Task(taskId, name, date, count, filial, fileName, tableName, isJur))
                        }
                        "Field_inf" -> {
                            var newtable = "CREATE TABLE $tableName (_id integer primary key autoincrement"
                            for (i in 0 until parser.attributeCount) {
                                newtable = newtable + "," + parser.getAttributeName(i) + " TEXT"
                                directoryDao.insert(Directory(0, taskId, parser.getAttributeName(i), parser.getAttributeValue(i)))
                            }
                            directoryDao.insert(Directory(0, taskId, "IsDone", ""))
                            newtable = "$newtable,IsDone TEXT)"
                            //newtable = "$newtable)"
                            sdb.execSQL(newtable)
                            sdb.execSQL(" CREATE INDEX numb_ind_$taskId ON $tableName (num)")
                        }
                        "Field_block_inf" -> {
                            for (i in 0 until parser.attributeCount) {
                                directoryDao.updateBlockInf(taskId, parser.getAttributeName(i), parser.getAttributeValue(i))
                            }
                        }
                        "Field_Block_name" -> {
                            for (i in 0 until parser.attributeCount) {
                                directoryDao.updateBlockName(taskId, parser.getAttributeName(i).substring(1), parser.getAttributeValue(i))
                            }
                        }
                        "Field_block_search" -> {
                            for (i in 0 until parser.attributeCount) {
                                if (parser.getAttributeName(i).isNotEmpty())
                                    directoryDao.updateBlockSearch(taskId, parser.getAttributeName(i), parser.getAttributeValue(i))
                            }
                        }
                        "Field__block_search_level" -> {
                            for (i in 0 until parser.attributeCount) {
                                if (parser.getAttributeName(i).isNotEmpty())
                                    directoryDao.updateLevel(taskId, parser.getAttributeName(i), parser.getAttributeValue(i))
                            }
                        }
                        "Field_block_sort" -> {
                            for (i in 0 until parser.attributeCount) {
                                if (parser.getAttributeName(i).isNotEmpty())
                                    directoryDao.updateSort(taskId, parser.getAttributeName(i), parser.getAttributeValue(i))
                            }
                        }
                        "Field_point_condition" -> {
                            while (parser.next() != XmlPullParser.END_TAG) {
                                if (parser.eventType != XmlPullParser.START_TAG) {
                                    continue
                                }
                                if (parser.name == "Field_block_source") {
                                    for (i in 0 until parser.attributeCount) {
                                        if (parser.getAttributeName(i).isNotEmpty())
                                            insertCatalog(
                                                    "4",
                                                    parser.getAttributeName(i).substring(5),
                                                    parser.getAttributeValue(i)
                                            )
                                    }
                                }
                            }
                        }
                        "Field_m_operators" -> {
                            while (parser.next() != XmlPullParser.END_TAG) {
                                if (parser.eventType != XmlPullParser.START_TAG) {
                                    continue;
                                }
                                if (parser.name == "Field_block_source") {
                                    for (i in 0 until parser.attributeCount) {
                                        if (parser.getAttributeName(i).isNotEmpty())
                                            insertCatalog(
                                                    "5",
                                                    parser.getAttributeName(i).substring(5),
                                                    parser.getAttributeValue(i)
                                            )
                                    }
                                }
                            }
                        }
                        "Field_source" -> {
                            while (parser.next() != XmlPullParser.END_TAG) {
                                if (parser.eventType != XmlPullParser.START_TAG) {
                                    continue;
                                }
                                if (parser.name == "Field_block_source") {
                                    for (i in 0 until parser.attributeCount) {
                                        if (parser.getAttributeName(i).isNotEmpty())
                                            insertCatalog(
                                                    "2",
                                                    parser.getAttributeName(i).substring(5),
                                                    parser.getAttributeValue(i)
                                            )
                                    }
                                }
                            }
                        }
                        "Field_block_prichina" -> {
                            for (i in 0 until parser.attributeCount) {
                                if (parser.getAttributeName(i).isNotEmpty())
                                    insertCatalog("3", parser.getAttributeName(i).substring(5), parser.getAttributeValue(i))
                            }
                        }
                        "data_inf" -> {
                            val cv = ContentValues()
                            for (i in 0 until parser.attributeCount) {
                                cv.put(parser.getAttributeName(i), parser.getAttributeValue(i).replace("'", "`"))
                            }
                            cv.put("IsDone", "Не виконано")
                            TestEntity.insertRows(sdb, tableName, cv)
                        }
                    }
                }
            }
            //TestEntity.getUsers(sdb, tableName)
            eventType = parser.next()
        }

        return "loaded"
    }

    private suspend fun insertCatalog(type: String, code: String, value: String) {
        val exists = catalogDao.getCatalogItem(type, code)
        if (!exists) {
            catalogDao.insert(Catalog(0, type, code, value))
        }
    }

    private fun writeLine(sb: StringBuffer, str: String) {
        sb.append(str)
        sb.append(System.getProperty("line.separator"));
    }

     suspend fun createXml(results: List<Result>, timings: List<Timing>) : String{
         var sb = StringBuffer()

             writeLine(sb, "<?xml version='1.0' encoding='windows-1251'?>")
             writeLine(sb, "<xml xmlns:s='uuid:BDC6E3F0-6DA3-11d1-A2A3-00AA00C14882'")
             writeLine(sb, "xmlns:dt='uuid:C2F41010-65B3-11d1-A29F-00AA00C14882'")
             writeLine(sb, "xmlns:rs='urn:schemas-microsoft-com:rowset'")
             writeLine(sb, "xmlns:z='#RowsetSchema'>")
             writeLine(sb, "<s:Schema id='RowsetSchema'>")
             writeLine(sb, "<s:ElementType name='row' content='eltOnly' rs:updatable='true'>")
             writeLine(
                 sb,
                 "<s:AttributeType name='Task_name' rs:number='1' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='Task_name'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='Dt_Crt' rs:number='2' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='Dt_Crt'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='12'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='TSzdn_id' rs:number='3' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='TSzdn_id'>")
             writeLine(
                 sb,
                 "<s:datatype dt:type='int' dt:maxLength='10' rs:precision='0' rs:fixedlength='true'/>"
             )
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='Filial' rs:number='4' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='Filial'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='10'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='Numb' rs:number='5' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='Numb'>")
             writeLine(
                 sb,
                 "<s:datatype dt:type='int' dt:maxLength='10' rs:precision='0' rs:fixedlength='true'/>"
             )
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='AccountID' rs:number='6' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='AccountID'>")
             writeLine(
                 sb,
                 "<s:datatype dt:type='int' dt:maxLength='10' rs:precision='0' rs:fixedlength='true'/>"
             )
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='DT_vpl' rs:number='7' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='DT_vpl'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='12'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='No_vpln' rs:number='8' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='No_vpln'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='12'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='Istochnk' rs:number='9' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='Istochnk'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='12'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='Pok_1' rs:number='10' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='Pok_1'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='12'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='Pok_2' rs:number='11' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='Pok_2'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='12'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='Pok_3' rs:number='12' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='Pok_3'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='12'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='Note' rs:number='13' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='Note'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='tel' rs:number='14' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='tel'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(sb, "<s:AttributeType name='DT_ins' rs:number='15' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'")
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='DT_ins'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='12'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='type' rs:number='16' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='type'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='Counter_numb' rs:number='17' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='Counter_numb'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='Zonnost' rs:number='18' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='Zonnost'>")
             writeLine(
                 sb,
                 "<s:datatype dt:type='int' dt:maxLength='10' rs:precision='0' rs:fixedlength='true'/>"
             )
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='Counter_capacity' rs:number='19' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='Counter_capacity'>")
             writeLine(
                 sb,
                 "<s:datatype dt:type='int' dt:maxLength='10' rs:precision='0' rs:fixedlength='true'/>"
             )
             writeLine(sb, "</s:AttributeType>")
             writeLine(
                 sb,
                 "<s:AttributeType name='Sred_rashod' rs:number='20' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
             )
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='Sred_rashod'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='12'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(sb, "<s:AttributeType name='lat' rs:number='21' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'")
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='lat'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='20'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(sb, "<s:AttributeType name='lng' rs:number='22' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'")
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='lng'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='20'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(sb, "<s:AttributeType name='numbpers' rs:number='23' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'")
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='numbpers'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='20'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(sb, "<s:AttributeType name='family' rs:number='24' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'")
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='family'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
             writeLine(sb, "</s:AttributeType>")
             writeLine(sb, "<s:AttributeType name='adress' rs:number='25' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'")
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='adress'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
             writeLine(sb, "</s:AttributeType>")

             writeLine(sb, "<s:AttributeType name='ismain_tel' rs:number='26' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'")
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='ismain_tel'>")
             writeLine(sb, "<s:datatype dt:type='int' dt:maxLength='1' rs:precision='0' rs:fixedlength='true'/>")
             writeLine(sb, "</s:AttributeType>")

             writeLine(sb, "<s:AttributeType name='point_condition' rs:number='27' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'")
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='adress'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='12'/>")
             writeLine(sb, "</s:AttributeType>")

             writeLine(sb, "<s:AttributeType name='old_tel' rs:number='28' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'")
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='adress'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
             writeLine(sb, "</s:AttributeType>")

             writeLine(sb, "<s:AttributeType name='photo' rs:number='29' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'")
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='photo'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
             writeLine(sb, "</s:AttributeType>")

             writeLine(sb, "<s:AttributeType name='startTaskTime' rs:number='30' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'")
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='startTaskTime'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
             writeLine(sb, "</s:AttributeType>")

             writeLine(sb, "<s:AttributeType name='firstEditDate' rs:number='31' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'")
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='firstEditDate'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
             writeLine(sb, "</s:AttributeType>")

             writeLine(sb, "<s:AttributeType name='editCount' rs:number='32' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'")
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='editCount'>")
             writeLine(sb, "<s:datatype dt:type='int' dt:maxLength='3' rs:precision='0' rs:fixedlength='true'/>")
             writeLine(sb, "</s:AttributeType>")

             writeLine(sb, "<s:AttributeType name='editSeconds' rs:number='33' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'")
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='editSeconds'>")
             writeLine(sb, "<s:datatype dt:type='int' dt:maxLength='10' rs:precision='0' rs:fixedlength='true'/>")
             writeLine(sb, "</s:AttributeType>")

             writeLine(sb, "<s:AttributeType name='lastEditDate' rs:number='34' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'")
             writeLine(sb, "rs:basetable='task_result' rs:basecolumn='lastEditDate'>")
             writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
             writeLine(sb, "</s:AttributeType>")

             writeLine(sb, "<s:extends type='rs:rowbase'/>")
             writeLine(sb, "</s:ElementType>")
             writeLine(sb, "</s:Schema>")
             writeLine(sb, "<rs:data>")

             for (i in results.indices) {
                 var uri: Uri ?= null
                 val photo = if(!results[i].photo.isNullOrEmpty()) {
                     uri = Uri.parse(results[i].photo)
                     "http://10.107.4.132/abon/"+results[i].filial+"/"+results[i].numbpers+"/"+uri.lastPathSegment
                 } else {
                     ""
                 }

                 val isMainTel = if(results[i].is_main.toString().isNullOrEmpty()) {
                     "0"
                 } else {
                     results[i].is_main
                 }

                 val oldNum = if(results[i].oldPhoneNumber.toString().isNullOrEmpty()) {
                     ""
                 } else {
                     results[i].oldPhoneNumber
                 }

                 val pointCondition = if(results[i].point_condition.isNullOrEmpty()) {
                     ""
                 } else {
                     results[i].point_condition
                 }

                 val counterNumb = if (results[i].counter.isNullOrEmpty()) {
                     "0"
                 } else {
                     results[i].counter
                 }

                 val zoneCount = if (results[i].zoneCount.isNullOrEmpty()) {
                     "0"
                 } else {
                     results[i].zoneCount
                 }

                 val counterCapacity = if (results[i].counterCapacity.isNullOrEmpty()) {
                     "0"
                 } else {
                     results[i].counterCapacity
                 }

                 if(!results[i].accountId.isNullOrEmpty()) {
                     writeLine(
                         sb, "<z:row  Task_name=" + "'" + results[i].taskName + "'" +
                                 " Dt_Crt=" + "'${results[i].createDate}'" +
                                 " TSzdn_id=" + "'${results[i].taskId}'" +
                                 " Filial=" + "'${results[i].filial}'" +
                                 " Numb='${results[i].num}'" +
                                 " AccountID='${results[i].accountId}'" +
                                 " DT_vpl='${results[i].doneDate}'" +
                                 " No_vpln='${results[i].notDone}'" +
                                 " Istochnk='${results[i].dataSource}'" +
                                 " Pok_1='${results[i].zone1}'" +
                                 " Pok_2='${results[i].zone2}'" +
                                 " pok_3='${results[i].zone3}'" +
                                 " Note='${results[i].note}'" +
                                 " tel='${results[i].phoneNumber}'" +
                                 " DT_ins='${results[i].insertDate}'" +
                                 " type='${results[i].type}'" +
                                 " Counter_numb='$counterNumb'" +
                                 " Zonnost='$zoneCount'" +
                                 " Counter_capacity='$counterCapacity'" +
                                 " Sred_rashod='${results[i].avgUsage}'" +
                                 " lat='${results[i].lat}'" +
                                 " lng='${results[i].lng}'" +
                                 " numbpers='${results[i].numbpers}'" +
                                 " family='${results[i].family}'" +
                                 " adress='${results[i].adress}'" +
                                 " ismain_tel='$isMainTel'" +
                                 " old_tel='$oldNum'" +
                                 " point_condition='$pointCondition'" +
                                 " photo='$photo'" +
                                 " startTaskTime='${timings[i].startTaskTime}'" +
                                 " firstEditDate='${timings[i].firstEditDate}'" +
                                 " editCount='${timings[i].editCount}'" +
                                 " editSeconds='${timings[i].editSeconds}'" +
                                 " lastEditDate='${timings[i].lastEditDate}'" +" />"
                     )
                 } else {
                     writeLine(
                         sb, "<z:row  Task_name=" + "'" + results[i].taskName + "'" +
                                 " Dt_Crt=" + "'${results[i].createDate}'" +
                                 " TSzdn_id=" + "'${results[i].taskId}'" +
                                 " Filial=" + "'${results[i].filial}'" +
                                 " Numb='${results[i].num}'" +
                                 " DT_vpl='${results[i].doneDate}'" +
                                 " No_vpln='${results[i].notDone}'" +
                                 " Istochnk='${results[i].dataSource}'" +
                                 " Pok_1='${results[i].zone1}'" +
                                 " Pok_2='${results[i].zone2}'" +
                                 " pok_3='${results[i].zone3}'" +
                                 " Note='${results[i].note}'" +
                                 " tel='${results[i].phoneNumber}'" +
                                 " DT_ins='${results[i].insertDate}'" +
                                 " type='${results[i].type}'" +
                                 " Counter_numb='$counterNumb'" +
                                 " Zonnost='$zoneCount'" +
                                 " Counter_capacity='$counterCapacity'" +
                                 " Sred_rashod='${results[i].avgUsage}'" +
                                 " lat='${results[i].lat}'" +
                                 " lng='${results[i].lng}'" +
                                 " numbpers='${results[i].numbpers}'" +
                                 " family='${results[i].family}'" +
                                 " adress='${results[i].adress}'" +
                                 " ismain_tel='$isMainTel'" +
                                 " old_tel='$oldNum'" +
                                 " point_condition='$pointCondition'" +
                                 " photo='$photo'" +
                                 " startTaskTime='${timings[i].startTaskTime}'" +
                                 " firstEditDate='${timings[i].firstEditDate}'" +
                                 " editCount='${timings[i].editCount}'" +
                                 " editSeconds='${timings[i].editSeconds}'" +
                                 " lastEditDate='${timings[i].lastEditDate}'" +" />"

                     )
                 }
             }
             writeLine(sb, "</rs:data>")
             writeLine(sb, "</xml>")

        return sb.toString()

    }

    private fun getNameFromUri(uri: Uri): String? {
        var filename: String? = ""
        val scheme = uri.scheme
        if (scheme == "file") {
            filename = uri.lastPathSegment
        } else if (scheme == "content") {
            val proj = arrayOf(OpenableColumns.DISPLAY_NAME)
            val cursor: Cursor? = context.contentResolver.query(uri, proj, null, null, null)
            if (cursor != null && cursor.count > 0 && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                filename = cursor.getString(nameIndex)
                cursor.close()
            }
            cursor?.close()
        }
        return filename
    }

    private fun isTableExists(tableName: String): Boolean {
        val query = "select DISTINCT tbl_name from sqlite_master where tbl_name = '$tableName'"
        database.query(query, null).use { cursor ->
                if (cursor.count > 0) {
                    return true
                }
            return false
        }
    }
}
