package ua.POE.Task_abon.data.repository

import android.content.ContentValues
import androidx.room.Ignore
import androidx.room.OnConflictStrategy
import androidx.sqlite.db.SupportSQLiteDatabase
import ua.POE.Task_abon.data.AppDatabase
import ua.POE.Task_abon.data.entities.TestEntity
import javax.inject.Inject


class TestEntityRepository @Inject constructor(appDatabase: AppDatabase) {

    private var sdb: SupportSQLiteDatabase = appDatabase.openHelper.readableDatabase

    fun getFieldsByBlock(tableName: String, fields: List<String>, num: Int) =
        TestEntity.getFieldsByBlock(sdb, tableName, fields, num)

    fun getTextByFields(tableName: String, fields: List<String>, num: Int) =
        TestEntity.getTextByFields(sdb, tableName, fields, num)

    fun getSearchedItemsByField(tableName: String, field: String) =
        TestEntity.getItemsByField(sdb, tableName, field)

    fun getCheckedConditions(taskId: Int, index: Int) =
        TestEntity.getCheckedConditions(sdb, taskId, index)


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
        TestEntity.dropTable(sdbw, taskId)
    }
}