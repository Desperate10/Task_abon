package ua.POE.Task_abon.data.repository

import ua.POE.Task_abon.data.dao.DirectoryDao
import javax.inject.Inject

class DirectoryRepository @Inject constructor(private val directoryDao: DirectoryDao) {

    suspend fun getBlockNames() = directoryDao.getFieldNames()

    fun getFieldsByBlockName(name:String, taskId : Int) = directoryDao.getFieldsByBlockName(name, taskId)

    suspend fun deleteDirectoryByTaskId(taskId: Int) = directoryDao.deleteByTaskId(taskId)

}