package ua.POE.Task_abon.data.dao.impl

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import androidx.room.Ignore
import androidx.room.OnConflictStrategy
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import ua.POE.Task_abon.data.AppDatabase
import ua.POE.Task_abon.data.dao.TaskCustomerDao
import ua.POE.Task_abon.data.entities.UserData
import java.lang.StringBuilder
import javax.inject.Inject

class TaskCustomerDaoImpl @Inject constructor(appDatabase: AppDatabase, private val taskCustomer: TaskCustomerDao) {

    private var sdb: SupportSQLiteDatabase = appDatabase.openHelper.readableDatabase

    fun getFieldsByBlock(taskId: Int, fields: List<String>, num: Int) =
        getFieldsByBlock(sdb, taskId, fields, num)

    fun getTextByFields(tableName: String, fields: List<String>, num: Int) =
        getSearchedItemsByField(sdb, tableName, fields, num)

    fun getSearchedItemsByField(tableName: String, field: String) =
        getItemsByField(sdb, tableName, field)

    fun getCheckedConditions(taskId: Int, index: Int) =
        getCheckedConditions(sdb, taskId, index)

    fun getUsers(taskId: Int, keys: List<String>, values:ArrayList<String>, status: String?) : List<UserData> {
        val whereSize = keys.size
        var whereClause = StringBuilder()
        var query = "SELECT * FROM TD$taskId "

        for(i in 0 until whereSize) {
            if(i ==0) query += "WHERE "
            whereClause = if (i != whereSize-1) {
                whereClause.append("${keys[i]} LIKE \"%${values[i]}%\" AND ")
            } else {
                whereClause.append("${keys[i]} LIKE \"%${values[i]}%\"")
            }
        }
        if (status.equals("Не виконано")) {
            if (whereSize>0) {
                whereClause.append(" AND IsDone = \"${status}\"")
            } else {
                whereClause.append(" WHERE IsDone = \"${status}\"")
            }
        }
        val st = query + whereClause.toString()
        //make flow from rawquery?
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
            csr.moveToFirst()
            do {
                for (i in fields.indices) {
                    val field = csr.getString(csr.getColumnIndex(fields[i]))
                    data[fields[i]] = field
                }
            } while (csr.moveToNext())
            csr.close()
            val sl = ArrayList<String>()
            for (i in fields.indices) {
                sl.add("\"${fields[i]}\"")
            }
            var sl1 = sl.toString().replace("[", "(")
            sl1 = sl1.replace("]", ")")
            csr =
                sdb.query("SELECT fieldName ,fieldNameTxt FROM directory WHERE fieldName in $sl1 AND taskId = $taskId")
            csr.moveToFirst()
            while (csr.moveToNext()) {
                data2[csr.getString(csr.getColumnIndex("fieldName"))] =
                    csr.getString(csr.getColumnIndex("fieldNameTxt"))
            }
            val common: HashMap<String, String> = HashMap()

            data.flatMap { dataEntry ->
                data2
                    .filterKeys { dataEntry.key == it }
                    .map { common[it.value] = dataEntry.value }
            }

            csr.close()

            return common
        }

        @SuppressLint("Range")
        @Ignore
        fun getSearchedItemsByField(
            sdb: SupportSQLiteDatabase,
            tableName: String,
            fields: List<String>,
            index: Int
        ): HashMap<String, String> {
            val csr: Cursor =
                sdb.query("SELECT ${fields.joinToString()} FROM $tableName WHERE _id = $index")
            val data: HashMap<String, String> = HashMap()
            csr.moveToFirst()
            do {
                for (i in fields.indices) {
                    val field = csr.getString(csr.getColumnIndex(fields[i]))
                    data[fields[i]] = field
                }
            } while (csr.moveToNext())
            csr.close()

            return data
        }

        @SuppressLint("Range")
        @Ignore
        fun getCheckedConditions(sdb: SupportSQLiteDatabase, taskId: Int, index: Int): String {
            var isExist = false
            val cursor1 = sdb.query("PRAGMA table_info('TD$taskId')", emptyArray())
            cursor1.moveToFirst()
            do {
                val currentColumn = cursor1.getString(1)
                if (currentColumn.equals("point_condition")) {
                    isExist = true
                }
            } while (cursor1.moveToNext())
            return if (isExist) {
                val csr: Cursor =
                    sdb.query("SELECT point_condition FROM TD$taskId WHERE _id = $index")
                csr.moveToFirst()
                val data = csr.getString(csr.getColumnIndex("point_condition"))
                csr.close()
                data
            } else {
                ""
            }
        }

        @SuppressLint("Range")
        @Ignore
        fun getItemsByField(
            sdb: SupportSQLiteDatabase,
            tableName: String,
            field: String
        ): ArrayList<String> {
            val csr: Cursor = sdb.query("SELECT DISTINCT $field FROM $tableName")
            val data: ArrayList<String> = ArrayList()

            csr.moveToFirst()
            do {
                data.add(csr.getString(csr.getColumnIndex(field)))
            } while (csr.moveToNext())
            csr.close()

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