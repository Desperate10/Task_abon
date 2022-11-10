package ua.POE.Task_abon.data.entities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.sqlite.db.SupportSQLiteDatabase


@Entity(tableName = "base")
data class TestEntity(@PrimaryKey var name: String) {

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
            var data: HashMap<String, String> = HashMap()
            var data2: HashMap<String, String> = HashMap()
            csr.moveToFirst()
            do {
                for (i in fields.indices) {
                    var field = csr.getString(csr.getColumnIndex(fields[i]))
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
        fun getTextByFields(
            sdb: SupportSQLiteDatabase,
            tableName: String,
            fields: List<String>,
            index: Int
        ): HashMap<String, String> {
            var csr: Cursor =
                sdb.query("SELECT ${fields.joinToString()} FROM $tableName WHERE _id = $index")
            var data: HashMap<String, String> = HashMap()
            csr.moveToFirst()
            do {
                for (i in fields.indices) {
                    var field = csr.getString(csr.getColumnIndex(fields[i]))
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
            var csr: Cursor = sdb.query("SELECT DISTINCT $field FROM $tableName")
            var data: ArrayList<String> = ArrayList()

            csr.moveToFirst()
            do {
                data.add(csr.getString(csr.getColumnIndex("$field")))
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


}