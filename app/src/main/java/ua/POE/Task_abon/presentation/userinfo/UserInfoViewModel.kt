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

    private var selectedSourceCode  = ""
    private var multiSpinnerSelectedFeatures = listOf<String>()
    private var checkDate = ""
    private var type = ""
    private var lastCount = ""
    private var counter = ""
    private var zoneCount = ""
    private var capacity = ""
    private var avgUsage = ""
    private var lastDate = ""
    private val sourceList = MutableStateFlow(emptyList<Catalog>())

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

    private val _selectedBlock = MutableStateFlow("Результати")
    val selectedBlock: StateFlow<String> = _selectedBlock

    val blockNames = flow {
        val blockNameList = mutableListOf<String>()
        blockNameList.addAll(directoryRepository.getBlockNames())
        blockNameList.add(0, "Результати")
        emit(blockNameList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Результати"))

    val featureList: StateFlow<List<Catalog>> = flow {
        val list = catalogDao.getFeatureList()
            .map { mapCatalogEntityToCatalog(it) }
        emit(list)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val selectedBlockData = _customerIndex
        .combine(_selectedBlock) { _, selectedBlock ->
            if (selectedBlock == "Результати") {
                getTechInfoTextByFields()
            } else {
                val fields = getFieldsByBlockName(selectedBlock)
                getTextFieldsByBlockName(fields)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val result = _customerIndex
        .flatMapLatest {
            getSavedData(it)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private fun getSavedData(index: Int): Flow<SavedData?> {
        return resultDao.getResultUser(taskId, index).map {
            mapResultToSavedData(it)
        }
    }

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

    private fun getSourceListFlow(position: Int) = flow {
        sourceList.value = if (position == 0) {
            catalogDao.getSourceList("2")
        } else {
            catalogDao.getSourceList("3")
        }.map { mapCatalogEntityToCatalog(it) }
        val sourceTextList = mutableListOf<String>()
        sourceTextList.add(0, "-Не вибрано-")
        sourceTextList.addAll(sourceList.value.map { it.text.toString() })
        emit(sourceTextList)
    }

    val getSourceList: StateFlow<List<String>> = statusSpinnerPosition
        .flatMapLatest { getSourceListFlow(it) }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val customerFeatures: StateFlow<List<KeyPairBoolData>> = _customerIndex
        .flatMapLatest {
            featureList.combine(result) { list, result ->
                setupFeatureSpinner(result?.pointCondition, list)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())


    private fun setupFeatureSpinner(
        condition: String?,
        featureList: List<Catalog>
    ): List<KeyPairBoolData> {
        val savedCondition = (condition ?: getCheckedConditions()).split(",").map { it.trim() }
        val conditionArray = mutableListOf<KeyPairBoolData>()
        for (feature in featureList) {
            if (feature.code.toString() in savedCondition) {
                conditionArray.add(KeyPairBoolData(feature.text!!, true))
            } else {
                conditionArray.add(KeyPairBoolData(feature.text!!, false))
            }
        }
        return conditionArray
    }

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

    fun saveResults(
        date: String,
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
        viewModelScope.launch(Dispatchers.Default) {
            resetTimer()
            val task: TaskEntity = getTask(taskId)
            val fields =
                listOf("num", "accountId", "Numbpers", "family", "Adress", "tel", "counpleas")
            val user =
                testEntityRepository.getTextByFields("TD$taskId", fields, customerIndex.value)

            val result = Result(
                task.name,
                task.date,
                taskId,
                task.filial,
                customerIndex.value,
                user["num"]!!,
                user["accountId"]!!,
                date,
                statusSpinnerPosition.value.toString(),
                selectedSourceCode,
                multiSpinnerSelectedFeatures.joinToString(),
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
    }

    private suspend fun getTask(taskId: Int) = taskRepository.getTask(taskId)

    fun getSourceName(code: String, type: String) = catalogDao.getSourceByCode(code, type)

    fun getOperatorsList() = catalogDao.getOperatorsList()

    fun getCheckedConditions() =
        testEntityRepository.getCheckedConditions(taskId, _customerIndex.value)

    fun setStatusSpinnerPosition(position: Int) {
        _statusSpinnerPosition.value = position
    }

    fun setSourceSpinnerPosition(position: Int) {
        _sourceSpinnerPosition.value = position
        getSelectedSourceCode(position)

    }

    private fun getSelectedSourceCode(position: Int) {
        selectedSourceCode = if (position != 0) {
            sourceList.value[position - 1].code!!
        } else ""
    }

    fun setSelectedCustomer(index: Int) {
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

    fun setItems(items: List<KeyPairBoolData>) {
        multiSpinnerSelectedFeatures = items.flatMap { item ->
            featureList.value
                .filter { item.name == it.text }
                .map { it.code.toString() }
        }
    }

}
