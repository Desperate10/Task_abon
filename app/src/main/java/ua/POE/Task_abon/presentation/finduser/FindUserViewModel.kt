package ua.POE.Task_abon.presentation.finduser

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import ua.POE.Task_abon.data.dao.DirectoryDao
import ua.POE.Task_abon.data.repository.TestEntityRepository

class FindUserViewModel @ViewModelInject constructor(val directoryDao: DirectoryDao, val testEntityRepository: TestEntityRepository) : ViewModel() {

    fun getSearchFieldsTxt(taskId: String) = directoryDao.getSearchFieldsTxt(taskId)

    private fun getSearchFieldNameByTxt(taskId: String, field: String) = directoryDao.getSearchFieldName(taskId, field)

    fun getSearchFieldValues(taskId: String, value: String) : ArrayList<String> {
        val fieldName = getSearchFieldNameByTxt(taskId, value)

        return testEntityRepository.getSearchedItemsByField("TD$taskId", fieldName)
    }
}