package ua.POE.Task_abon.data.dao

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import androidx.room.Ignore
import androidx.room.OnConflictStrategy
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import ua.POE.Task_abon.data.AppDatabase
import ua.POE.Task_abon.presentation.model.CustomerMainData
import java.util.SortedMap
import javax.inject.Inject

/**
 * We are not creating room entity for tasks because every new task has new set of data
 * We creating table for every task dynamically, and working with data by yourself
 * */
class TaskDataDaoImpl @Inject constructor(
    appDatabase: AppDatabase,
    private val taskCustomer: TaskCustomerDao
) {
    private var sdb: SupportSQLiteDatabase = appDatabase.openHelper.readableDatabase

    /**
     * Each field belongs to different block of data (such as "Technical", "Results", etc.)
     * @param taskId getting fields belongs only to current taskId
     * @param fields list of fields of selected block
     * @param num getting fields belongs only to current user
     * */
    fun getFieldsByBlock(taskId: Int, fields: List<String>, num: Int) =
        getFieldsByBlock(sdb, taskId, fields, num)

    /**
     * Function to get value of fields in list
     * @param tableName getting fields belongs only to current table
     * @param fields list of fields which value we are searching for
     * @param num getting fields belongs only to current user
     * */
    fun getFieldsValue(tableName: String, fields: List<String>, num: Int) =
        getFieldsValue(sdb, tableName, fields, num)

    /**
     * Function to get value of specified field for customers filtering purposes
     * @param tableName getting field belongs only to current table
     * @param field list of fields which value we are searching for
     * */
    fun getSearchFieldValue(tableName: String, field: String) =
        getFieldValue(sdb, tableName, field)

    /**
     * Function to get value of point_condition field of current user
     * @param taskId getting fields belongs only to current task
     * @param index id of customer
     * */
    fun getCustomerPointCondition(taskId: Int, index: Int) =
        getPointConditions(sdb, taskId, index)

    /**
     * Function to get customers
     * @param taskId getting fields belongs only to current task
     * @param keys list of selected fields
     * @param values list of selected fields values
     * @param status customer's isDone status
     * */
    suspend fun getCustomers(
        taskId: Int,
        keys: List<String>,
        values: ArrayList<String>,
        status: String?
    ): List<CustomerMainData> {
        val whereSize = keys.size
        var whereClause = StringBuilder()
        var query = "SELECT * FROM TD$taskId "


        for (i in 0 until whereSize) {
            if (i == 0) query += "WHERE "
            whereClause = if (i != whereSize - 1) {
                whereClause.append("${keys[i]} LIKE \"%${values[i]}%\" AND ")
            } else {
                whereClause.append("${keys[i]} LIKE \"%${values[i]}%\"")
            }
        }
        if (status.equals("Не виконано")) {
            if (whereSize > 0) {
                whereClause.append(" AND IsDone = \"${status}\"")
            } else {
                whereClause.append(" WHERE IsDone = \"${status}\"")
            }
        }
        val st = query + whereClause.toString()
        return taskCustomer.getSearchedCustomersList(SimpleSQLiteQuery(st))
    }

    companion object {

        @SuppressLint("Range")
        @Ignore
        fun getFieldsByBlock(
            sdb: SupportSQLiteDatabase,
            taskId: Int,
            fields: List<String>,
            index: Int
        ): HashMap<String, String> {
            var csr: Cursor =
                sdb.query("SELECT ${fields.joinToString()} FROM TD$taskId WHERE _id = $index")
            val data: HashMap<String, String> = HashMap()
            val data2: HashMap<String, String> = HashMap()
            csr.use { csr ->
                csr.moveToFirst()
                do {
                    for (i in fields.indices) {
                        val field = csr.getString(csr.getColumnIndex(fields[i]))
                        data[fields[i]] = field
                    }
                } while (csr.moveToNext())
            }
            val sl = ArrayList<String>()
            for (i in fields.indices) {
                sl.add("\"${fields[i]}\"")
            }
            var sl1 = sl.toString().replace("[", "(")
            sl1 = sl1.replace("]", ")")
            csr =
                sdb.query("SELECT fieldName ,fieldNameTxt FROM directory WHERE fieldName in $sl1 AND taskId = $taskId")
            val common: HashMap<String, String> = HashMap()
            csr.use { csr ->
                csr.moveToFirst()
                while(csr.moveToNext()) {
                //
                   // Log.d("testim", csr.getString(csr.getColumnIndex("fieldNameTxt")))
                    data2[csr.getString(csr.getColumnIndex("fieldName"))] =
                        csr.getString(csr.getColumnIndex("fieldNameTxt"))
                }
                data.flatMap { dataEntry ->
                    data2
                        .filterKeys { dataEntry.key == it }
                        .map { common[it.value] = dataEntry.value }
                }
            }

            return common//.toSortedMap(naturalOrder())
        }

        @SuppressLint("Range")
        @Ignore
        fun getFieldsValue(
            sdb: SupportSQLiteDatabase,
            tableName: String,
            fields: List<String>,
            index: Int
        ): HashMap<String, String> {
            val csr: Cursor =
                sdb.query("SELECT ${fields.joinToString()} FROM $tableName WHERE _id = $index")
            val data: HashMap<String, String> = HashMap()
            csr.use {
                it.moveToFirst()
                do {
                    for (i in fields.indices) {
                        val field = it.getString(it.getColumnIndex(fields[i]))
                        data[fields[i]] = field
                    }
                } while (it.moveToNext())
            }

            return data
        }

        @SuppressLint("Range")
        @Ignore
        fun getPointConditions(sdb: SupportSQLiteDatabase, taskId: Int, index: Int): String {
            var isExist = false
            val cursor = sdb.query("PRAGMA table_info('TD$taskId')", emptyArray())
            cursor.use { cursor ->
                cursor.moveToFirst()
                do {
                    val currentColumn = cursor.getString(1)
                    if (currentColumn.equals("point_condition")) {
                        isExist = true
                    }
                } while (cursor.moveToNext())
            }
            return if (isExist) {
                val csr: Cursor =
                    sdb.query("SELECT point_condition FROM TD$taskId WHERE _id = $index")
                csr.use { csr ->
                    csr.moveToFirst()
                    val data = csr.getString(csr.getColumnIndex("point_condition"))
                    data
                }
            } else {
                ""
            }
        }

        @SuppressLint("Range")
        @Ignore
        fun getFieldValue(
            sdb: SupportSQLiteDatabase,
            tableName: String,
            field: String
        ): ArrayList<String> {
            val csr: Cursor = sdb.query("SELECT DISTINCT $field FROM $tableName")
            val data: ArrayList<String> = ArrayList()
            csr.use { csr ->
                csr.moveToFirst()
                do {
                    data.add(csr.getString(csr.getColumnIndex(field)))
                } while (csr.moveToNext())
            }
            return data
        }

        @Ignore
        fun insertRows(sdb: SupportSQLiteDatabase, tableName: String?, cv: ContentValues): Long? {
            return tableName?.let { sdb.insert(it, OnConflictStrategy.IGNORE, cv) }
        }

        @Ignore
        fun dropTable(sdb: SupportSQLiteDatabase, taskId: Int) {
            sdb.execSQL("DROP TABLE IF EXISTS TD$taskId")
        }

    }


    private var sdbw: SupportSQLiteDatabase = appDatabase.openHelper.writableDatabase

    fun setDone(taskId: Int, num: String) {
        val values = ContentValues()
        values.put("IsDone", "Виконано")

        sdbw.update("TD$taskId", OnConflictStrategy.REPLACE, values, "num=$num", null)

    }

    fun setUnDone(taskId: Int) {
        val values = ContentValues()
        values.put("IsDone", "Не виконано")

        sdbw.update("TD$taskId", OnConflictStrategy.REPLACE, values, "", null)
    }

    fun deleteTable(taskId: Int) {
        dropTable(sdbw, taskId)
    }
}