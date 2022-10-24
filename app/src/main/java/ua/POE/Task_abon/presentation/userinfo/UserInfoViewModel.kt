package ua.POE.Task_abon.presentation.userinfo

import android.util.Log
import android.view.View
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidbuts.multispinnerfilter.KeyPairBoolData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.POE.Task_abon.R
import ua.POE.Task_abon.data.dao.CatalogDao
import ua.POE.Task_abon.data.dao.ResultDao
import ua.POE.Task_abon.data.entities.Directory
import ua.POE.Task_abon.data.entities.Result
import ua.POE.Task_abon.data.entities.TaskEntity
import ua.POE.Task_abon.data.entities.Timing
import ua.POE.Task_abon.data.mapper.mapCatalogEntityToCatalog
import ua.POE.Task_abon.data.mapper.mapResultToSavedData
import ua.POE.Task_abon.data.repository.DirectoryRepository
import ua.POE.Task_abon.data.repository.TaskRepository
import ua.POE.Task_abon.data.repository.TestEntityRepository
import ua.POE.Task_abon.data.repository.TimingRepository
import ua.POE.Task_abon.domain.model.*
import ua.POE.Task_abon.utils.getNeededEmojis
import ua.POE.Task_abon.utils.mapLatestIterable
import java.lang.Thread.State
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val directoryRepository: DirectoryRepository,
    private val taskRepository: TaskRepository,
    private val testEntityRepository: TestEntityRepository,
    private val timingRepository: TimingRepository,
    private val resultDao: ResultDao,
    private val catalogDao: CatalogDao
) : ViewModel() {

    private var checkDate = ""
    private var type = ""
    private var lastCount = ""
    private var counter = ""
    private var zoneCount = ""
    private var capacity = ""
    private var avgUsage = ""
    private var lastDate = ""
    private var sourceList = listOf<Catalog>()
    var featureList = listOf<Catalog>()

    private val taskId =
        savedStateHandle.get<Int>("taskId") ?: throw RuntimeException("taskId is null")
    private var index =
        savedStateHandle.get<Int>("id") ?: throw RuntimeException("Customer index is null")
    private val taskCustomerQuantity =
        savedStateHandle.get<Int>("count") ?: throw RuntimeException("Customer's quantity is null ")

    private val _statusSpinnerPosition = MutableStateFlow(0)
    val statusSpinnerPosition: StateFlow<Int> = _statusSpinnerPosition
    private val _sourceSpinnerPosition = MutableStateFlow(0)
    val sourceSpinnerPosition: StateFlow<Int> = _sourceSpinnerPosition

    private val _customerIndex = MutableStateFlow(index)
    val customerIndex: StateFlow<Int> = _customerIndex

    private val _blockNames = MutableStateFlow(listOf("Результати"))
    val blockNames: StateFlow<List<String>> = _blockNames

    private val _selectedBlock = MutableStateFlow("Результати")
    val selectedBlock: StateFlow<String> = _selectedBlock

    val selectedBlockData = _customerIndex
        .combine(_selectedBlock) { id, selectedBlock ->
            if (selectedBlock == "Результати") {
                getTechInfoTextByFields()
            } else {
                val fields = getFieldsByBlockName(selectedBlock)
                getTextFieldsByBlockName(fields)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val result = _customerIndex
        .combine(_selectedBlock) { id, selectedBlock ->
            if (selectedBlock == "Результати") {
                getSavedData()
            } else {
                null
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
        Log.d("testim", savedStateHandle.keys().toString())
        startTimer()
        getBlockNames()
        getFeatureList()
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

    private val currentSelectedSourceCode: StateFlow<String> =
        sourceSpinnerPosition.map {
            if (it != 0) {
                Log.d("testim", sourceList[it - 1].code!!)
                sourceList[it - 1].code!!
            } else ""
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), "")


    private fun getSourceListFlow(position: Int) = flow {
        sourceList = if (position == 0) {
            catalogDao.getSourceList("2")
        } else {
            catalogDao.getSourceList("3")
        }.map { mapCatalogEntityToCatalog(it) }
        val sourceTextList = mutableListOf<String>()
        sourceTextList.add(0, "-Не вибрано-")
        sourceTextList.addAll(sourceList.map { it.text.toString() })
        emit(sourceTextList)
    }

    val getSourceList: StateFlow<List<String>> = statusSpinnerPosition
        .flatMapLatest { getSourceListFlow(it) }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private fun getBlockNames() {
        viewModelScope.launch {
            val blockNameList = mutableListOf<String>()
            blockNameList.addAll(directoryRepository.getBlockNames())
            blockNameList.add(0, "Результати")
            _blockNames.value = blockNameList
        }
    }

    private fun getCustomerFeatures(condition: String?) = flow {
        emit((condition ?: getCheckedConditions()).split(",").map { it.trim() })
    }

    private fun initFeatureSpinnerList(customerFeatures: List<String>) = flow {
        val conditionArray = mutableListOf<KeyPairBoolData>()
        for (feature in featureList) {
            if (feature.code.toString() in customerFeatures) {
                conditionArray.add(KeyPairBoolData(feature.text!!, true))
            } else {
                conditionArray.add(KeyPairBoolData(feature.text!!, false))
            }
        }
        emit(conditionArray)
    }

    val resultil = result.flatMapLatest { getCustomerFeatures(result.value?.pointCondition)}
        .flatMapLatest { initFeatureSpinnerList(it) }
        //.combine(featureList.asFlow()) сделать его отдельным как стейтфлоу?



    /*private fun getCustomerFeatures(condition: SavedData?) = flow  {
        val data = if (condition == null) {
            getCheckedConditions()
        } else {
            condition.pointCondition
        }?.split(",")?.map { it.trim() }

        val conditionArray = mutableListOf<KeyPairBoolData>()

        for (feature in featureList) {
            if (data != null) {
                if (feature.code.toString() in data) {
                    conditionArray.add(KeyPairBoolData(feature.text!!, true))
                } else {
                    conditionArray.add(KeyPairBoolData(feature.text!!, false))
                }
            }
        }
    }*/

    val preloadResultTab = _customerIndex
        .flatMapLatest {
            selectedBlock
        }
        .flatMapLatest {
            getTechInfo()
        }

    fun getTechInfo() = flow {
        val techHash = getTechInfoTextByFields()
        val controlInfo = StringBuilder()

        techHash.forEach { (key, value) ->
            when (key) {
                "TimeZonalId" -> {
                    zoneCount = value
                }
                "Lastdate" -> {
                    lastDate = value
                }
                "Lastlcount" -> {
                    lastCount = value
                }
                "srnach" -> {
                    avgUsage = value
                }
                "type" -> {
                    type = value
                }
                "Counter_numb" -> {
                    counter = value
                }
                "Rozr" -> {
                    capacity = value
                }
                "contr_date" -> {
                    checkDate = value
                    controlInfo.append(" $value")
                }
                "contr_pok" -> {
                    controlInfo.append(" $value")
                }
                "contr_name" -> {
                    controlInfo.append(" $value")
                }
            }
        }
        emit(
            TechInfo(
                zoneCount = zoneCount,
                lastDate = lastDate,
                lastCount = lastCount,
                averageUsage = avgUsage,
                type = type,
                capacity = capacity,
                checkDate = checkDate,
                inspector = controlInfo.toString()
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), TechInfo())

    fun getCustomerBasicInfo(icons: ArrayList<Icons>) =
        flow {
            val basicInfoFieldsList = ArrayList<String>()
            val basicFields = directoryRepository.getBasicFields(taskId)
            basicInfoFieldsList.addAll(basicFields)
            basicInfoFieldsList.add("Counter_numb")
            val tdHash = getTextFieldsByBlockName(basicInfoFieldsList)
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
                            if (value.isNotEmpty())
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
        }

    /*fun getBasicInfo(fields: List<String>, tableName: String, num: Int) =
        testEntityRepository.getBasicInfoBlock(fields, tableName, num)*/

    fun getFieldsByBlockName(name: String): List<String> =
        directoryRepository.getFieldsByBlockName(name, taskId)

    fun getTextFieldsByBlockName(fields: List<String>) =
        testEntityRepository.getFieldsByBlock(taskId, fields, index)

    fun getTechInfoTextByFields(): HashMap<String, String> {
        val fields = getFieldsByBlockName("Тех.информация")
        return testEntityRepository.getTextByFields("TD$taskId", fields, index)
    }

    //save date when pressing saveResult
    fun saveEditTiming(firstEditDate: String, date: String) {
        viewModelScope.launch {
            //_isTrueEdit.value = true
            if (timingRepository.isStartTaskDateEmpty(taskId, index.toString())) {
                timingRepository.insertTiming(
                    Timing(
                        taskId,
                        index.toString(),
                        firstEditDate,
                        date,
                        "",
                        0,
                        0,
                        ""
                    )
                )
            } else if (timingRepository.isFirstEditDateEmpty(taskId, index.toString())) {
                timingRepository.updateFirstEditDate(taskId, index.toString(), firstEditDate)
                timingRepository.updateEditCount(taskId, index.toString(), 1)
                saveEndEditDate(taskId, index.toString(), date)
                saveEditTime(taskId, index.toString(), time)
            } else {
                //adding +1 to edit count
                saveEditTime(taskId, index.toString(), time)
                saveEndEditDate(taskId, index.toString(), date)
                timingRepository.upEditCount(taskId, index.toString())

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
        date: String,
        source2: String,
        zone1: String,
        zone2: String,
        zone3: String,
        note: String,
        phoneNumber: String,
        is_main: Int,
        lat: String,
        lng: String,
        photo: String?
    ) {
        resetTimer()
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
            statusSpinnerPosition.value.toString(),
            currentSelectedSourceCode.value,
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
            personalAccount,
            user["family"],
            user["Adress"],
            photo,
            user["counpleas"]
        )
        resultDao.insertNewData(result)

        testEntityRepository.setDone(taskId, user["num"]!!)
    }

    private suspend fun getTask(taskId: Int) = taskRepository.getTask(taskId)

    //ошибка
    suspend fun getSavedData() =
        mapResultToSavedData(
            resultDao.getResultUser(taskId, index)
        )

    fun getSourceName(code: String, type: String) = catalogDao.getSourceByCode(code, type)

    fun getOperatorsList() = catalogDao.getOperatorsList()

    fun getCheckedConditions() =
        testEntityRepository.getCheckedConditions(taskId, index)

    fun getFeatureList() {
        viewModelScope.launch {
            featureList = catalogDao.getSourceList("4")
                .map {
                    mapCatalogEntityToCatalog(it)
                }
        }
    }

    fun setStatusSpinnerPosition(position: Int) {
        _statusSpinnerPosition.value = position
    }

    fun setSourceSpinnerPosition(position: Int) {
        _sourceSpinnerPosition.value = position
    }

    fun setSelectedCustomer(index: Int) {
        savedStateHandle["num"] = index
        _customerIndex.value = index
    }

    fun setSelectedBlock(blockName: String) {
        _selectedBlock.value = blockName
    }

    fun selectPreviousCustomer() {
        index = if (index != 1) {
            index.minus(1)
        } else {
            taskCustomerQuantity
        }
        setSelectedCustomer(index)
    }

    fun selectNextCustomer() {
        index = if (index != taskCustomerQuantity) {
            index.plus(1)
        } else {
            1
        }
        setSelectedCustomer(index)
    }

    //сбрасываем таймер при переключении юзера
    private fun resetTimer() {
        time = 0
    }


    //adding
    /*fun getSourceList(position: Int): Flow<List<Catalog>> {
        return if (position == 0) {
            catalogDao.getSourceList("2")
        } else {
            catalogDao.getSourceList("3")
        }.mapLatestIterable { mapCatalogEntityToCatalog(it) }
            .flowOn(Dispatchers.Default)
    }*/

    /*fun getSourceList(type: String): List<Catalog> {
        return catalogDao.getSourceList(type).map {
            mapCatalogEntityToCatalog(it)
        }
    }*/

    /*fun getFeatureList(): StateFlow<List<Catalog>> {
       return catalogDao.getSourceList("4")
           .mapLatestIterable { mapCatalogEntityToCatalog(it) }
           .flowOn(Dispatchers.Default)
           .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
   }*/

}
