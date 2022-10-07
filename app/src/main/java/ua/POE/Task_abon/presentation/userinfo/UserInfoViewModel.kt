package ua.POE.Task_abon.presentation.userinfo

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ua.POE.Task_abon.data.dao.CatalogDao
import ua.POE.Task_abon.data.dao.ResultDao
import ua.POE.Task_abon.data.entities.*
import ua.POE.Task_abon.data.mapper.mapCatalogEntityToCatalog
import ua.POE.Task_abon.data.repository.DirectoryRepository
import ua.POE.Task_abon.data.repository.TaskRepository
import ua.POE.Task_abon.data.repository.TestEntityRepository
import ua.POE.Task_abon.data.repository.TimingRepository
import ua.POE.Task_abon.domain.model.Catalog
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UserInfoViewModel @ViewModelInject constructor(
    private val directoryRepository: DirectoryRepository,
    private val taskRepository: TaskRepository,
    private val testEntityRepository: TestEntityRepository,
    private val timingRepository: TimingRepository,
    private val resultDao: ResultDao,
    private val catalogDao: CatalogDao
) : ViewModel() {

    private var _isTrueEdit = MutableLiveData<Boolean>()
    val isTrueEdit: MutableLiveData<Boolean> = _isTrueEdit

    var time = 0
    private val timer = Timer()

    init {
        _isTrueEdit.value = false
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    time++
                 //   Log.d("testim", time.toString())
                }
            },0, 1000)
        }
    }

    fun getBlockNames(): MutableList<String> = directoryRepository.getBlockNames()

    fun getFieldsByBlockName(name: String, taskId: Int): List<Directory> =
        directoryRepository.getFieldsByBlockName(name, taskId)

    fun getTextFieldsByBlockName(fields: List<String>, tableName: String, num: Int) =
        testEntityRepository.getFieldsByBlock(tableName, fields, num)

//    fun getTextByFields(fields: List<String>, tableName: String, num : Int) = testEntityRepository.getTextByFields(tableName, fields, num)

    fun getTechInfoTextByFields(taskId: Int, index: Int): HashMap<String, String> {
        val tech = ArrayList<String>()
        val fields = getFieldsByBlockName("Тех.информация", taskId)
        for (element in fields) {
            element.fieldName?.let { tech.add(it) }
        }
        return testEntityRepository.getTextByFields("TD$taskId", tech, index)
    }

    //save date when pressing saveResult
    fun saveEditTiming(taskId: Int, num: String, firstEditDate: String, date: String) {
        viewModelScope.launch {
            _isTrueEdit.value = true
            if (timingRepository.isStartTaskDateEmpty(taskId, num)) {
                timingRepository.insertTiming(
                    Timing(
                        taskId,
                        num,
                        firstEditDate,
                        date,
                        "",
                        0,
                        0,
                        ""
                    )
                )
            } else if (timingRepository.isFirstEditDateEmpty(taskId, num)) {
                timingRepository.updateFirstEditDate(taskId, num, firstEditDate)
                timingRepository.updateEditCount(taskId, num, 1)
                saveEndEditDate(taskId, num, date)
                saveEditTime(taskId, num, time)
            } else {
                //adding +1 to edit count
                saveEditTime(taskId, num, time)
                saveEndEditDate(taskId, num, date)
                timingRepository.upEditCount(taskId, num)

            }
        }
    }

    //save date when finish editing task when onStop() userInfoFragment
    private fun saveEndEditDate(taskId: Int, num: String, date: String) {
        viewModelScope.launch {
            if (!timingRepository.isFirstEditDateEmpty(taskId, num)) {
                timingRepository.updateLastEditDate(taskId, num, date)
            }
        }
    }

    private fun saveEditTime(taskId: Int, num: String, time: Int) {
        viewModelScope.launch {
            val seconds: Int = timingRepository.getEditTime(taskId, num)
            val newEditTime = seconds + time
            timingRepository.updateEditSeconds(taskId, num, newEditTime)
        }
    }

    suspend fun saveResults(
        taskId: Int,
        index: Int,
        date: String,
        isDone: String,
        source: String,
        source2: String,
        zone1: String,
        zone2: String,
        zone3: String,
        note: String,
        phoneNumber: String,
        is_main: Int,
        type: String,
        counter: String,
        zoneCount: String,
        capacity: String,
        avgUsage: String,
        lat: String,
        lng: String,
        numbpers: String,
        family: String,
        adress: String,
        photo: String?
    ) {

        val task: TaskEntity = getTask(taskId)
        val fields = listOf("num", "accountId", "Numbpers", "family", "Adress", "tel", "counpleas")
        val user = testEntityRepository.getTextByFields("TD$taskId", fields, index)

        val result = Result(
            task.name,
            task.date,
            taskId,
            task.filial,
            index,
            user["num"]!!,
            user["accountId"]!!,
            date,
            isDone,
            source,
            source2,
            zone1,
            zone2,
            zone3,
            note,
            user["tel"]!!,
            phoneNumber,
            is_main,
            "",
            type,
            counter,
            zoneCount,
            capacity,
            avgUsage,
            lat,
            lng,
            numbpers,
            family,
            adress,
            photo,
            user["counpleas"]
        )
        resultDao.insertNewData(result)

        testEntityRepository.setDone(taskId, user["num"]!!)
    }

    private suspend fun getTask(taskId: Int) = taskRepository.getTask(taskId)

    fun getResult(taskId: Int, index: Int) = resultDao.getResultUser(taskId, index)

    fun getSourceList(type: String): List<Catalog> {
        return catalogDao.getSourceList(type).map {
            mapCatalogEntityToCatalog(it)
        }
    }

    fun getSourceName(code: String, type: String) = catalogDao.getSourceByCode(code, type)

    fun getNoteName(code: String) = catalogDao.getSourceNoteByCode(code)

    fun getOperatorsList() = catalogDao.getOperatorsList()

    fun getCheckedConditions(taskId: Int, index: Int) =
        testEntityRepository.getCheckedConditions(taskId, index)

}
