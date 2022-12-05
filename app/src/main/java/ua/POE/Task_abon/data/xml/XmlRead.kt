package ua.POE.Task_abon.data.xml

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import ua.POE.Task_abon.data.AppDatabase
import ua.POE.Task_abon.data.dao.CatalogDao
import ua.POE.Task_abon.data.dao.DirectoryDao
import ua.POE.Task_abon.data.dao.TaskDao
import ua.POE.Task_abon.data.dao.TaskDataDaoImpl
import ua.POE.Task_abon.data.entities.CatalogEntity
import ua.POE.Task_abon.data.entities.DirectoryEntity
import ua.POE.Task_abon.data.entities.TaskEntity
import ua.POE.Task_abon.utils.XmlResult
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * Populate database from xml file
 * */
class XmlRead @Inject constructor(
    private val context: Context,
    private val database: AppDatabase,
    private val taskDao: TaskDao,
    private val directoryDao: DirectoryDao,
    private val catalogDao: CatalogDao
) {
    suspend operator fun invoke(uri: Uri): XmlResult {

        val fileName = getNameFromUri(uri)
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        try {
            val parser: XmlPullParser = factory.newPullParser()
            withContext(Dispatchers.IO) {
                parser.setInput( //StringReader(fileContent)
                    BufferedReader(
                        InputStreamReader(
                            context.contentResolver.openInputStream(uri),
                            "windows-1251"
                        )
                    )
                )
            }
            var eventType = parser.eventType
            var taskId = 0
            var tableName = ""
            val sdb: SupportSQLiteDatabase = database.openHelper.writableDatabase
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (tagName) {
                            "Task_st" -> {
                                val name = parser.getAttributeValue(0)
                                val date = parser.getAttributeValue(1)
                                taskId = parser.getAttributeValue(2).toInt()
                                tableName = "TD$taskId"
                                if (isTableExists(tableName)) {
                                    return XmlResult.Fail("Вже існує обхідний лист з цим ідентифікатором!")
                                }
                                // addTable(tableName)
                                val count = parser.getAttributeValue(5)
                                var filial = ""
                                var isJur = "0"
                                if (parser.attributeCount == 8) filial = parser.getAttributeValue(7)
                                if (parser.attributeCount == 9) isJur =
                                    (count.toInt() - parser.getAttributeValue(8).toInt()).toString()
                                taskDao.insert(
                                    TaskEntity(
                                        taskId,
                                        name,
                                        date,
                                        count,
                                        filial,
                                        fileName,
                                        tableName,
                                        isJur
                                    )
                                )
                            }
                            "Field_inf" -> {
                                var newtable =
                                    "CREATE TABLE $tableName (_id integer primary key autoincrement"
                                for (i in 0 until parser.attributeCount) {
                                    newtable = newtable + "," + parser.getAttributeName(i) + " TEXT"
                                    directoryDao.insert(
                                        DirectoryEntity(
                                            0,
                                            taskId,
                                            parser.getAttributeName(i),
                                            parser.getAttributeValue(i)
                                        )
                                    )
                                }
                                directoryDao.insert(DirectoryEntity(0, taskId, "IsDone", ""))
                                newtable = "$newtable,IsDone TEXT)"
                                //newtable = "$newtable)"
                                sdb.execSQL(newtable)
                                sdb.execSQL(" CREATE INDEX numb_ind_$taskId ON $tableName (num)")
                            }
                            "Field_block_inf" -> {
                                for (i in 0 until parser.attributeCount) {
                                    directoryDao.updateBlockInfo(
                                        taskId,
                                        parser.getAttributeName(i),
                                        parser.getAttributeValue(i)
                                    )
                                }
                            }
                            "Field_Block_name" -> {
                                for (i in 0 until parser.attributeCount) {
                                    directoryDao.updateBlockName(
                                        taskId,
                                        parser.getAttributeName(i).substring(1),
                                        parser.getAttributeValue(i)
                                    )
                                }
                            }
                            "Field_block_search" -> {
                                for (i in 0 until parser.attributeCount) {
                                    if (parser.getAttributeName(i).isNotEmpty())
                                        directoryDao.updateBlockSearch(
                                            taskId,
                                            parser.getAttributeName(i),
                                            parser.getAttributeValue(i)
                                        )
                                }
                            }
                            "Field__block_search_level" -> {
                                for (i in 0 until parser.attributeCount) {
                                    if (parser.getAttributeName(i).isNotEmpty())
                                        directoryDao.updateLevel(
                                            taskId,
                                            parser.getAttributeName(i),
                                            parser.getAttributeValue(i)
                                        )
                                }
                            }
                            "Field_block_sort" -> {
                                for (i in 0 until parser.attributeCount) {
                                    if (parser.getAttributeName(i).isNotEmpty())
                                        directoryDao.updateSort(
                                            taskId,
                                            parser.getAttributeName(i),
                                            parser.getAttributeValue(i)
                                        )
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
                                        continue
                                    }
                                    if (parser.name == "Field_block_source") {
                                        insertCatalog(
                                            "2",
                                            "0",
                                            "-Не вибрано-"
                                        )
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
                                insertCatalog(
                                    "3",
                                    "0",
                                    "-Не вибрано-"
                                )
                                for (i in 0 until parser.attributeCount) {
                                    if (parser.getAttributeName(i).isNotEmpty())
                                        insertCatalog(
                                            "3",
                                            parser.getAttributeName(i).substring(5),
                                            parser.getAttributeValue(i)
                                        )
                                }
                            }
                            "data_inf" -> {
                                val cv = ContentValues()
                                for (i in 0 until parser.attributeCount) {
                                    cv.put(
                                        parser.getAttributeName(i),
                                        parser.getAttributeValue(i).replace("'", "`")
                                    )
                                }
                                cv.put("IsDone", "Не виконано")
                                TaskDataDaoImpl.insertRows(sdb, tableName, cv)
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            return XmlResult.Fail("У файлі є помилки")
        }
        return XmlResult.Success("Лист успішно додано")
    }

    private suspend fun insertCatalog(type: String, code: String, value: String) {
        val isExist = catalogDao.isCatalogItemExists(type, code)
        if (!isExist) {
            catalogDao.insert(CatalogEntity(0, type, code, value))
        }
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
