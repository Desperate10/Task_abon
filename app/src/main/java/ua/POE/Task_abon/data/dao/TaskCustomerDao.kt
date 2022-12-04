package ua.POE.Task_abon.data.dao

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import ua.POE.Task_abon.presentation.model.CustomerMainData

@Dao
interface TaskCustomerDao {

    /**
     * Query user's main data
     * Implementation in TaskDataDaoImpl
     * */
    @RawQuery
    suspend fun getSearchedCustomersList(query: SimpleSQLiteQuery) : List<CustomerMainData>
}