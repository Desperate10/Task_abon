package ua.POE.Task_abon.data.xml

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import ua.POE.Task_abon.BuildConfig
import ua.POE.Task_abon.data.dao.ResultDao
import ua.POE.Task_abon.data.dao.TimingDao
import ua.POE.Task_abon.data.entities.ResultEntity
import ua.POE.Task_abon.data.entities.TimingEntity
import ua.POE.Task_abon.utils.XmlResult
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.Writer
import javax.inject.Inject

/**
 * Create import xml file
 * */
class XmlWrite @Inject constructor(
    private val context: Context,
    private val result: ResultDao,
    private val timing: TimingDao
) {

    suspend operator fun invoke(taskId: Int, uri: Uri): XmlResult {
        try {
            val os = context.contentResolver.openOutputStream(uri)
            withContext(Dispatchers.IO) {
                val results = result.getResult(taskId)
                val timings = timing.getTiming(taskId)
                val w: Writer = BufferedWriter(OutputStreamWriter(os, "windows-1251"))
                val xml = composeXml(results, timings)
                w.write(xml)
                w.flush()
                w.close()
            }
        } catch (e: Exception) {
            return XmlResult.Fail("Помилка у створенні файлу")
        }
        return XmlResult.Success("Файл створено!")
    }

    private fun composeXml(results: List<ResultEntity>, timings: List<TimingEntity>): String {
        val sb = StringBuffer()
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
        writeLine(
            sb,
            "<s:AttributeType name='DT_ins' rs:number='15' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
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
        writeLine(
            sb,
            "<s:AttributeType name='lat' rs:number='21' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='lat'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='20'/>")
        writeLine(sb, "</s:AttributeType>")
        writeLine(
            sb,
            "<s:AttributeType name='lng' rs:number='22' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='lng'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='20'/>")
        writeLine(sb, "</s:AttributeType>")
        writeLine(
            sb,
            "<s:AttributeType name='numbpers' rs:number='23' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='numbpers'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='20'/>")
        writeLine(sb, "</s:AttributeType>")
        writeLine(
            sb,
            "<s:AttributeType name='family' rs:number='24' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='family'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
        writeLine(sb, "</s:AttributeType>")
        writeLine(
            sb,
            "<s:AttributeType name='adress' rs:number='25' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='adress'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='ismain_tel' rs:number='26' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='ismain_tel'>")
        writeLine(
            sb,
            "<s:datatype dt:type='int' dt:maxLength='1' rs:precision='0' rs:fixedlength='true'/>"
        )
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='point_condition' rs:number='27' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='adress'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='12'/>")
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='old_tel' rs:number='28' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='adress'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='photo' rs:number='29' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='photo'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='startTaskTime' rs:number='30' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='startTaskTime'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='firstEditDate' rs:number='31' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='firstEditDate'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='editCount' rs:number='32' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='editCount'>")
        writeLine(
            sb,
            "<s:datatype dt:type='int' dt:maxLength='10' rs:precision='0' rs:fixedlength='true'/>"
        )
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='editSeconds' rs:number='33' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='editSeconds'>")
        writeLine(
            sb,
            "<s:datatype dt:type='int' dt:maxLength='10' rs:precision='0' rs:fixedlength='true'/>"
        )
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='lastEditDate' rs:number='34' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='lastEditDate'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='endTaskTime' rs:number='35' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='endTaskTime'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='counpleas' rs:number='36' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='counpleas'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='50'/>")
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='Ident_code' rs:number='37' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='Ident_code'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='20'/>")
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='Physical_PersonId' rs:number='38' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='Physical_PersonId'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='10'/>")
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='pillar_checked' rs:number='39' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='pillar_checked'>")
        writeLine(
            sb,
            "<s:datatype dt:type='int' dt:maxLength='10' rs:precision='0' rs:fixedlength='true'/>"
        )
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='new_pillar_number' rs:number='40' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='new_pillar_number'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='10'/>")
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='new_pillar_number_descr' rs:number='41' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='new_pillar_number_descr'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='100'/>")
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='new_pillar_lat' rs:number='42' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='new_pillar_lat'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='100'/>")
        writeLine(sb, "</s:AttributeType>")

        writeLine(
            sb,
            "<s:AttributeType name='new_pillar_lng' rs:number='43' rs:nullable='true' rs:writeunknown='true' rs:basecatalog='DB_UTILITY'"
        )
        writeLine(sb, "rs:basetable='task_result' rs:basecolumn='new_pillar_lng'>")
        writeLine(sb, "<s:datatype dt:type='string' rs:dbtype='str' dt:maxLength='100'/>")
        writeLine(sb, "</s:AttributeType>")

        writeLine(sb, "<s:extends type='rs:rowbase'/>")
        writeLine(sb, "</s:ElementType>")
        writeLine(sb, "</s:Schema>")
        writeLine(sb, "<rs:data>")

        for (i in results.indices) {
            var uri: Uri?
            val photo = if (!results[i].photo.isNullOrEmpty()) {
                uri = Uri.parse(results[i].photo)
                BuildConfig.MY_PHOTO_STORAGE + results[i].filial + "/" + results[i].numbpers + "/" + uri.lastPathSegment
            } else {
                ""
            }

            val accountId = StringUtils.defaultIfBlank(results[i].accountId.toString(), "0")
            val isMainTel = StringUtils.defaultIfBlank(results[i].isMain.toString(), "0")
            val oldNum = StringUtils.defaultIfBlank(results[i].oldPhoneNumber.toString(), "")
            val pointCondition =
                StringUtils.defaultIfBlank(results[i].pointCondition.toString(), "")
            val counterNumb = StringUtils.defaultIfBlank(results[i].counter.toString(), "0")
            val zoneCount = StringUtils.defaultIfBlank(results[i].zoneCount.toString(), "0")
            val counterCapacity =
                StringUtils.defaultIfBlank(results[i].counterCapacity.toString(), "0")
            val physicalPersonId = StringUtils.defaultIfBlank(results[i].physicalPersonId, "0")

            writeLine(
                sb, "<z:row  Task_name=" + "'" + results[i].taskName + "'" +
                        " Dt_Crt=" + "'${results[i].createDate}'" +
                        " TSzdn_id=" + "'${results[i].taskId}'" +
                        " Filial=" + "'${results[i].filial}'" +
                        " Numb='${results[i].num}'" +
                        " AccountID='$accountId'" +
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
                        " endTaskTime='${timings[i].endTaskTime}'" +
                        " firstEditDate='${timings[i].firstEditDate}'" +
                        " editCount='${timings[i].editCount}'" +
                        " editSeconds='${timings[i].editSeconds}'" +
                        " lastEditDate='${timings[i].lastEditDate}'" +
                        " counpleas='${results[i].counterPlace}'" +
                        " Ident_code='${results[i].identificationCode}'" +
                        " Physical_PersonId='$physicalPersonId'" +
                        " pillar_checked='${results[i].pillarChecked}'" +
                        " new_pillar_number='${results[i].newPillar}'" +
                        " new_pillar_number_descr='${results[i].newPillarDescription}'" +
                        " new_pillar_lat='${results[i].pillarLat}'" +
                        " new_pillar_lng='${results[i].pillarLng}'" +
                        " />"
            )
        }
        writeLine(sb, "</rs:data>")
        writeLine(sb, "</xml>")

        return sb.toString()
    }

    private fun writeLine(sb: StringBuffer, str: String) {
        sb.append(str)
        sb.append(System.getProperty("line.separator"));
    }
}