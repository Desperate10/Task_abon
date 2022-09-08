package ua.POE.Task_abon.ui.userinfo

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ua.POE.Task_abon.data.dao.CatalogDao
import ua.POE.Task_abon.data.dao.ResultDao
import ua.POE.Task_abon.data.entities.Directory
import ua.POE.Task_abon.data.entities.Result
import ua.POE.Task_abon.data.entities.Task
import ua.POE.Task_abon.data.repository.DirectoryRepository
import ua.POE.Task_abon.data.repository.TaskRepository
import ua.POE.Task_abon.data.repository.TestEntityRepository

class UserInfoViewModel @ViewModelInject constructor(private val directoryRepository: DirectoryRepository,private val taskRepository: TaskRepository, private val testEntityRepository: TestEntityRepository, private val resultDao: ResultDao, private val catalogDao: CatalogDao) : ViewModel() {

    private var _isTrueEdit = MutableLiveData<Boolean>()
    val isTrueEdit: MutableLiveData<Boolean> = _isTrueEdit

    init {
        _isTrueEdit.value = false
    }

    fun getBlockNames() : MutableList<String> = directoryRepository.getBlockNames()

    fun getFieldsByBlockName(name: String, taskId: String) : List<Directory> = directoryRepository.getFieldsByBlockName(name, taskId)

    fun getTextFieldsByBlockName(fields: List<String>, tableName: String, num : Int) = testEntityRepository.getFieldsByBlock(tableName, fields, num)

//    fun getTextByFields(fields: List<String>, tableName: String, num : Int) = testEntityRepository.getTextByFields(tableName, fields, num)

    fun getTechInfoTextByFields(taskId: String, index: Int) : HashMap<String, String> {
        val tech = ArrayList<String>()
        val fields = getFieldsByBlockName("Тех.информация", taskId)
        for (element in fields) {
            element.fieldName?.let { tech.add(it) }
        }
        return testEntityRepository.getTextByFields("TD$taskId", tech, index)
    }

    /*//save date when pressing saveResult
    suspend fun saveStartEditDate(taskId: String, date: String) {
        viewModelScope.launch {
            _isTrueEdit.value = true
            if (resultDao.checkStartTaskDate(taskId).isNullOrEmpty()) {
                resultDao.saveStartTaskDate(date)
                resultDao.saveEditCount(0)

            } else if (resultDao.checkFirstEditDate(taskId).isNullOrEmpty()) {
                resultDao.saveFirstEditDate(date)
                resultDao.saveEditCount(1)
            } else {
                //adding +1 to edit count
                resultDao.upEditCount()
            }
        }
    }

    //save date when finish editing task when onStop() userInfoFragment
    suspend fun saveEndEditDate(taskId: String, date: String) {
        viewModelScope.launch {
            resultDao.saveLastEditDate(date)
        }
    }

    suspend fun saveEditTime(taskId: String, time: Int) {
        viewModelScope.launch {
            val seconds: Int = resultDao.getEditTime(taskId)
            val newEditTime = seconds + time
            resultDao.saveEditTime(taskId, newEditTime)
        }
    }*/

    suspend fun saveResults(taskId: String, index: Int, date: String, isDone: String, source: String, source2:String, zone1: String, zone2: String, zone3: String, note: String,phoneNumber: String, is_main: Int, type: String, counter: String, zoneCount: String, capacity: String, avgUsage: String,lat: String, lng:String, numbpers: String, family: String, adress: String, photo:String?) {

            val task: Task = getTask(taskId)
            val fields = listOf("num", "accountId", "Numbpers", "family", "Adress", "tel")
            val user = testEntityRepository.getTextByFields("TD$taskId", fields, index)

            val result = Result(task.name, task.date, taskId, task.filial,index, user["num"]!!, user["accountId"]!!, date, isDone, source, source2, zone1, zone2, zone3, note, user["tel"]!!, phoneNumber, is_main, "", type, counter, zoneCount, capacity, avgUsage, lat, lng, numbpers, family, adress, photo)
            resultDao.insertNewData(result)

            testEntityRepository.setDone(taskId, user["num"]!!)
    }

    private suspend fun getTask(taskId: String) = taskRepository.getTask(taskId)

    fun getResult(taskId: String, index: Int) = resultDao.getResultUser(taskId, index)

    fun getSourceList(type: String) = catalogDao.getSourceList(type)

    fun getSourceName(code: String, type: String) = catalogDao.getSourceByCode(code, type)

    fun getNoteName(code: String) = catalogDao.getSourceNoteByCode(code)

    fun getOperatorsList() = catalogDao.getOperatorsList()

    fun getCheckedConditions(taskId: String, index: Int) = testEntityRepository.getCheckedConditions(taskId, index)

}
