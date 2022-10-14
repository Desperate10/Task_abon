package ua.POE.Task_abon.presentation.userinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.POE.Task_abon.data.dao.CatalogDao
import ua.POE.Task_abon.data.dao.ResultDao
import ua.POE.Task_abon.data.entities.Directory
import ua.POE.Task_abon.data.entities.Result
import ua.POE.Task_abon.data.entities.TaskEntity
import ua.POE.Task_abon.data.entities.Timing
import ua.POE.Task_abon.data.mapper.mapCatalogEntityToCatalog
import ua.POE.Task_abon.data.repository.DirectoryRepository
import ua.POE.Task_abon.data.repository.TaskRepository
import ua.POE.Task_abon.data.repository.TestEntityRepository
import ua.POE.Task_abon.data.repository.TimingRepository
import ua.POE.Task_abon.domain.model.BasicInfo
import ua.POE.Task_abon.domain.model.Catalog
import ua.POE.Task_abon.domain.model.Icons
import ua.POE.Task_abon.utils.getNeededEmojis
import ua.POE.Task_abon.utils.mapLatestIterable
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val directoryRepository: DirectoryRepository,
    private val taskRepository: TaskRepository,
    private val testEntityRepository: TestEntityRepository,
    private val timingRepository: TimingRepository,
    private val resultDao: ResultDao,
    private val catalogDao: CatalogDao
) : ViewModel() {

    val statusSpinnerPosition = MutableStateFlow(0)
    val sourceSpinnerPosition = MutableStateFlow(0)
    val customerIndex = MutableStateFlow(1)

    private val _blockNames = MutableStateFlow(listOf("Результати"))
    val blockNames: StateFlow<List<String>> = _blockNames

    private var basicInfoFields = listOf<String>()

    private var personalAccount = ""
    private var personalAccountKey = ""
    private var personalAccountEmoji = ""
    private var address = ""
    private var name = ""
    private var counterKey = ""
    private var counterValue = ""
    private var counterEmoji = ""

    private val timer = Timer()
    var time = 0

    init {
        startTimer()
        getBlockNames()
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }

    private fun startTimer() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                timer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        time++
                    }
                }, 0, 1000)
            }
        }
    }

    private fun getBlockNames() {
        viewModelScope.launch {
            val blockNameList = mutableListOf<String>()
            blockNameList.addAll(directoryRepository.getBlockNames())
            blockNameList.add(0, "Результати")
            _blockNames.value = blockNameList
        }
    }

    fun getCustomerBasicInfo(taskId: Int, index: Int, icons: ArrayList<Icons>) =
        flow {
            val basicInfoFieldsList = ArrayList<String>()
            val basicFields = directoryRepository.getBasicFields(taskId)
            basicInfoFieldsList.addAll(basicFields)
            basicInfoFieldsList.add("Counter_numb")
            val tdHash = getTextFieldsByBlockName(basicInfoFieldsList, "TD$taskId", index)
            var pillar = ""
            val otherInfo = StringBuilder()
            tdHash.forEach { (key, value) ->
                if (key.isNotEmpty()) {
                    when (key) {
                        "О/р" -> {
                            personalAccountKey = key
                            personalAccount = value
                        }
                        "icons_account" -> {
                            val text = getNeededEmojis(icons, value)
                            personalAccountEmoji = "$personalAccount $text"
                        }
                        "Адреса" -> {
                            address = value
                        }
                        "ПІБ" -> {
                            name = value
                        }
                        "Опора" -> {
                            pillar = "Оп.$value"
                        }
                        "№ ліч." -> {
                            counterValue = value
                            counterEmoji = "$counterValue $counterKey"
                        }
                        "icons_counter" -> {
                            counterKey = getNeededEmojis(icons, value)
                        }
                        else -> {
                            if(value.isNotEmpty())
                            otherInfo.append("$value ")
                        }
                    }
                }
            }
            otherInfo.append(pillar).toString()
            emit(
                BasicInfo(
                    personalAccount = personalAccountEmoji,
                    address = address,
                    name = name,
                    counter = counterEmoji,
                    other = otherInfo.toString()
                )
            )
        }.flowOn(Dispatchers.Default).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            BasicInfo("","","", "", "")
        )

    fun getBasicInfo(fields: List<String>, tableName: String, num: Int) =
        testEntityRepository.getBasicInfoBlock(fields, tableName, num)

    fun getFieldsByBlockName(name: String, taskId: Int): List<Directory> =
        directoryRepository.getFieldsByBlockName(name, taskId)

    fun getTextFieldsByBlockName(fields: List<String>, tableName: String, num: Int) =
        testEntityRepository.getFieldsByBlock(tableName, fields, num)

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
            //_isTrueEdit.value = true
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

    //adding
    fun getSourceList(): StateFlow<List<Catalog>> {
        return if (statusSpinnerPosition.value == 0) {
            catalogDao.getSourceList("2")
        } else {
            catalogDao.getSourceList("3")
        }.mapLatestIterable { mapCatalogEntityToCatalog(it) }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    /*fun getSourceList(type: String): List<Catalog> {
        return catalogDao.getSourceList(type).map {
            mapCatalogEntityToCatalog(it)
        }
    }*/

    fun getSourceName(code: String, type: String) = catalogDao.getSourceByCode(code, type)

    fun getNoteName(code: String) = catalogDao.getSourceNoteByCode(code)

    fun getOperatorsList() = catalogDao.getOperatorsList()

    fun getCheckedConditions(taskId: Int, index: Int) =
        testEntityRepository.getCheckedConditions(taskId, index)

    fun getFeatureList(): StateFlow<List<Catalog>> {
        return catalogDao.getSourceList("4")
            .mapLatestIterable { mapCatalogEntityToCatalog(it) }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

}
