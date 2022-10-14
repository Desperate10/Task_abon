package ua.POE.Task_abon.presentation.finduser

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.POE.Task_abon.data.dao.DirectoryDao
import ua.POE.Task_abon.data.repository.TestEntityRepository
import javax.inject.Inject

@HiltViewModel
class FindUserViewModel @Inject constructor(val directoryDao: DirectoryDao, val testEntityRepository: TestEntityRepository) : ViewModel() {

    fun getSearchFieldsTxt(taskId: Int) = directoryDao.getSearchFieldsTxt(taskId)

    private fun getSearchFieldNameByTxt(taskId: Int, field: String) = directoryDao.getSearchFieldName(taskId, field)

    fun getSearchFieldValues(taskId: Int, value: String) : ArrayList<String> {
        val fieldName = getSearchFieldNameByTxt(taskId, value)

        return testEntityRepository.getSearchedItemsByField("TD$taskId", fieldName)
    }
}