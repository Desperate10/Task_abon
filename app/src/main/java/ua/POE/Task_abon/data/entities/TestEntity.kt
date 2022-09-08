package ua.POE.Task_abon.data.entities

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase


@Entity(tableName = "base")
data class TestEntity(@PrimaryKey var name: String, var value: String? = "") {

    companion object {

    const val BASETABLE_NAME = "base"
    const val BASETABLE_COL_NAME = "name"
    const val BASETABLE_COL_VALUE = "value"

    const val BASETABLE_NAME_PLACEHOLDER = ":tablename:"

    const val BASETABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS $BASETABLE_NAME_PLACEHOLDER" +
            "(" +
              "$BASETABLE_COL_NAME TEXT PRIMARY KEY," +
              "$BASETABLE_COL_VALUE TEXT)"

        @Ignore
        fun getFieldsByBlock(sdb: SupportSQLiteDatabase, tableName: String, fields: List<String>, index: Int) : HashMap<String, String> {
            var csr : Cursor = sdb.query("SELECT ${fields.joinToString()} FROM $tableName WHERE _id = $index")
            var data : HashMap<String, String> = HashMap()
            var data2 : HashMap<String, String> = HashMap()
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
            csr = sdb.query("SELECT fieldName ,fieldNameTxt FROM directory WHERE fieldName in $sl1 AND taskId = ${tableName.substring(2)}")
            csr.moveToFirst()
            while (csr.moveToNext()) {
                data2[csr.getString(csr.getColumnIndex("fieldName"))] = csr.getString(csr.getColumnIndex("fieldNameTxt"))
            }
            var common: HashMap<String, String> = HashMap()
            for (key in data.keys) {
                for (key1 in data2.keys) {
                    if (key == key1) {
                        common[data2[key1]!!] = data[key]!!
                    }
                }
            }

            csr.close()

            return common
        }

        @Ignore
        fun getTextByFields(sdb: SupportSQLiteDatabase, tableName: String, fields: List<String>, index: Int) : HashMap<String, String> {
            var csr : Cursor = sdb.query("SELECT ${fields.joinToString()} FROM $tableName WHERE _id = $index")
            var data : HashMap<String, String> = HashMap()
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

        @Ignore
        fun getCheckedConditions(sdb: SupportSQLiteDatabase, tableName: String, index: Int) :String{
            var isExist = false
            val cursor1 = sdb.query("PRAGMA table_info('TD$tableName')", null)
            cursor1.moveToFirst()
            do {
                val currentColumn = cursor1.getString(1)
                if (currentColumn.equals("point_condition")) {
                    isExist = true
                }
            } while (cursor1.moveToNext())
            return if (isExist) {
                val csr: Cursor =
                    sdb.query("SELECT point_condition FROM TD$tableName WHERE _id = $index")
                csr.moveToFirst()
                val data = csr.getString(csr.getColumnIndex("point_condition"))
                csr.close()
                data
            } else {
                ""
            }

            /*val cursor1 = sdb.query("SELECT COUNT(*) FROM pragma_table_info('TD$tableName') WHERE name='point_condition'")
            cursor1.moveToFirst()
            val count = cursor1.getString(0)
            return if (count.equals("0")) "" else {
                val csr: Cursor =
                    sdb.query("SELECT point_condition FROM TD$tableName WHERE _id = $index")
                csr.moveToFirst()
                val data = csr.getString(csr.getColumnIndex("point_condition"))
                csr.close()
                data
            }*/
        }

        @Ignore
        fun getItemsByField(sdb: SupportSQLiteDatabase, tableName: String, field: String) : ArrayList<String> {
            var csr : Cursor = sdb.query("SELECT DISTINCT $field FROM $tableName")
            var data : ArrayList<String> =  ArrayList()

            csr.moveToFirst()
            do {
                data.add(csr.getString(csr.getColumnIndex("$field")))
            } while (csr.moveToNext())
            csr.close()

            return data
        }

        @Ignore
        fun insertRows(sdb: SupportSQLiteDatabase, tableName: String?, cv: ContentValues): Long? {

            return sdb.insert(tableName, OnConflictStrategy.IGNORE, cv)
        }

        @Ignore
        fun dropTable(sdb: SupportSQLiteDatabase, taskId : String) {
            sdb.execSQL("DROP TABLE IF EXISTS TD$taskId")
        }

    }





}