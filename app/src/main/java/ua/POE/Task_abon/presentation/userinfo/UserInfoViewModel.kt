package ua.POE.Task_abon.presentation.userinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidbuts.multispinnerfilter.KeyPairBoolData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ua.POE.Task_abon.data.dao.*
import ua.POE.Task_abon.data.dao.impl.TaskCustomerDaoImpl
import ua.POE.Task_abon.data.entities.Result
import ua.POE.Task_abon.data.entities.TaskEntity
import ua.POE.Task_abon.data.entities.Timing
import ua.POE.Task_abon.data.mapper.mapCatalogEntityToCatalog
import ua.POE.Task_abon.data.mapper.mapResultToSavedData
import ua.POE.Task_abon.domain.model.BasicInfo
import ua.POE.Task_abon.domain.model.Catalog
import ua.POE.Task_abon.domain.model.SavedData
import ua.POE.Task_abon.domain.model.TechInfo
import ua.POE.Task_abon.presentation.model.Icons
import ua.POE.Task_abon.utils.getNeededEmojis
import ua.POE.Task_abon.utils.mapLatestIterable
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val icons: List<Icons>,
    private val savedStateHandle: SavedStateHandle,
    private val directory: DirectoryDao,
    private val task: TaskDao,
    private val customer: TaskCustomerDaoImpl,
    private val timing: TimingDao,
    private val result: ResultDao,
    private val catalog: CatalogDao
) : ViewModel() {

    private var isResultSaved = false
    private var startEditTime: String = ""
    private var selectedSourceCode = ""
    private var multiSpinnerSelectedFeatures = listOf<String>()
    private var checkDate = ""
    private var type = ""
    private var lastCount = ""
    private var counter = ""
    private var zoneCount = ""
    private var capacity = ""
    private var avgUsage = ""
    private var lastDate = ""
    private var personalAccount = ""
    private var personalAccountKey = ""
    private var personalAccountEmoji = ""
    private var address = ""
    private var name = ""
    private var counterKey = ""
    private var counterValue = ""
    private var counterEmoji = ""
    private var sourceList: List<Catalog>? = null

    private val dateAndTime = "dd.MM.yyyy HH:mm:ss"
    private val dateAndTimeFormat = SimpleDateFormat(dateAndTime, Locale.getDefault())

    private val taskId =
        savedStateHandle.get<Int>("taskId") ?: throw NullPointerException("taskId is null")
    private var index =
        savedStateHandle.get<Int>("id") ?: throw NullPointerException("Customer index is null")
    private val taskCustomerQuantity =
        savedStateHandle.get<Int>("count")
            ?: throw NullPointerException("Customer's quantity is null ")

    private val operators = MutableStateFlow<List<String>>(emptyList())

    private val _statusSpinnerPosition = MutableStateFlow(0)
    val statusSpinnerPosition: StateFlow<Int> = _statusSpinnerPosition

    private val sourceSpinnerPosition = MutableStateFlow(0)

    private val _techInfo = MutableStateFlow<Map<String, String>>(emptyMap())

    private val _customerIndex = MutableStateFlow(index)
    val customerIndex: StateFlow<Int> = _customerIndex

    private val _selectedBlock = MutableStateFlow("Результати")
    val selectedBlock: StateFlow<String> = _selectedBlock

    private val _saveAnswer = MutableStateFlow("")
    val saveAnswer: StateFlow<String> = _saveAnswer

    private val _sources = MutableStateFlow<List<String>>(emptyList())
    val sources: StateFlow<List<String>> = _sources

    private val timer = Timer()
    private var time = 0

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

    val blockNames = flow {
        val blockNameList = mutableListOf<String>()
        blockNameList.add(0, "Результати")
        emit(blockNameList)
        blockNameList.addAll(directory.getBlockNames())
        emit(blockNameList)
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), listOf("Результати"))

    val selectedBlockData = _customerIndex
        .combine(_selectedBlock) { _, selectedBlock ->
            if (selectedBlock == "Результати") {
                updateTechInfoMap()
                _techInfo.value
            } else {
                val fields = getFieldsByBlockName(selectedBlock)
                getTextFieldsByBlockName(fields)
            }
        }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    val savedData = _customerIndex
        .combine(_statusSpinnerPosition) { index, status ->
            getSavedData(index, status)
        }
        .flowOn(Dispatchers.Main)
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 2)

    private suspend fun getSavedData(index: Int, status: Int): SavedData {
        val savedData = mapResultToSavedData(result.getResultUser(taskId, index))
        updateSourceList(status)

        val type = if (status == 0) {
            "2"
        } else {
            "3"
        }

        val sourceName = if (!savedData.source.isNullOrEmpty()) {
            getSourceName(savedData.source, type)
        } else {
            ""
        }

        return savedData.copy(source = sourceName)
    }

    private suspend fun updateSourceList(status: Int) {
        _sources.value = if (status == 0) {
            catalog.getSourceList("2")
        } else {
            catalog.getSourceList("3")
        }
            .map { mapCatalogEntityToCatalog(it) }
            .map { it.text.toString() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val customerFeatures: StateFlow<List<KeyPairBoolData>> = _customerIndex
        .flatMapLatest {
            featureList.combine(savedData) { list, result ->
                setupFeatureSpinner(result.pointCondition, list)
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val featureList =
        catalog.getFeatureList().mapLatestIterable { mapCatalogEntityToCatalog(it) }
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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

    @OptIn(ExperimentalCoroutinesApi::class)
    val preloadResultTab = _customerIndex
        .flatMapLatest {
            selectedBlock
        }
        .mapLatest {
            updateTechInfoMap()
        }
        .mapLatest { showTechInfo() }
        .flowOn(Dispatchers.IO)

    private fun showTechInfo(): TechInfo {
        val controlInfo = StringBuilder()

        _techInfo.value.forEach { (key, value) ->
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
        return TechInfo(
            zoneCount = zoneCount,
            lastDate = lastDate,
            lastCount = lastCount,
            averageUsage = avgUsage,
            type = type,
            capacity = capacity,
            checkDate = checkDate,
            inspector = controlInfo.toString()
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val basicInfo = _customerIndex
        .mapLatest {
            getCustomerBasicInfo(this, icons)
        }.filter { it.name.isNotEmpty() }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private fun getTextFieldsByBlockName(fields: List<String>) =
        customer.getFieldsByBlock(taskId, fields, _customerIndex.value)

    private suspend fun getFieldsByBlockName(name: String): List<String> =
        directory.getFieldsByBlockName(name, taskId)

    private suspend fun updateTechInfoMap() {
        val fields = getFieldsByBlockName("Тех.информация")
        _techInfo.value =
            customer.getTextByFields("TD$taskId", fields, _customerIndex.value)
    }

    //save date when pressing saveResult
    private fun saveEditTiming(firstEditDate: String, date: String) {
        viewModelScope.launch {
            coroutineScope {
                if (timing.getStartTaskDate(taskId, _customerIndex.value).isNullOrEmpty()) {
                    timing.insertTiming(
                        Timing(
                            taskId = taskId,
                            num = _customerIndex.value,
                            startTaskTime = firstEditDate,
                            endTaskTime = date,
                            "",
                            0,
                            0,
                            ""
                        )
                    )
                } else if (timing.getFirstEditDate(taskId, _customerIndex.value).isNullOrEmpty()) {
                    timing.updateFirstEditDate(
                        taskId,
                        _customerIndex.value,
                        firstEditDate
                    )
                    timing.updateEditCount(taskId, _customerIndex.value, 1)
                    timing.updateLastEditDate(taskId, _customerIndex.value, date)
                    saveEditTime(taskId, _customerIndex.value, time)
                } else {
                    //adding +1 to edit count
                    saveEditTime(taskId, _customerIndex.value, time)
                    timing.updateLastEditDate(taskId, _customerIndex.value, date)
                    timing.upEditCount(taskId, _customerIndex.value)
                }
                resetTimer()
            }
        }
    }

    private suspend fun saveEditTime(taskId: Int, num: Int, time: Int) {
        val seconds: Int = timing.getEditTime(taskId, num)
        val newEditTime = seconds + time
        timing.updateEditSeconds(taskId, num, newEditTime)
    }

    fun saveResults(
        date: String,
        zone1: String,
        zone2: String,
        zone3: String,
        note: String,
        phoneNumber: String,
        isMainPhone: Boolean,
        lat: String,
        lng: String,
        photo: String
    ) {
        viewModelScope.launch {
            if (phoneNumber.isNotEmpty() && (phoneNumber.take(3) !in operators.value || phoneNumber.length < 10)) {
                _saveAnswer.value = "Неправильний формат номера телефону"
            } else if (statusSpinnerPosition.value == 1 && sourceSpinnerPosition.value == 0) {
                _saveAnswer.value = "Ви забули вказати джерело"
            } else if (zone1.isNotEmpty() || (statusSpinnerPosition.value == 1 && sourceSpinnerPosition.value != 0)) {
                val task: TaskEntity = getTask(taskId)
                val fields =
                    listOf("num", "accountId", "Numbpers", "family", "Adress", "tel", "counpleas")
                val user =
                    customer.getTextByFields("TD$taskId", fields, customerIndex.value)
                val isMainPhoneInt = if (isMainPhone) 1 else 0
                val photoValid = if (photo.length > 4) {
                    photo
                } else {
                    null
                }

                val saveData = Result(
                    task.name,
                    task.date,
                    taskId,
                    task.filial,
                    _customerIndex.value,
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
                    isMainPhoneInt,
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
                    photoValid,
                    user["counpleas"]
                )

                val currentDateAndTime =
                    dateAndTimeFormat.format(Date())
                saveEditTiming(startEditTime, currentDateAndTime)
                result.insertNewData(saveData)
                customer.setDone(taskId, user["num"]!!)
                setResultSavedState(true)
                _saveAnswer.value = "Результати збережено"
            } else {
                _saveAnswer.value = "Ви не ввели нові показники"
            }
        }
    }

    private suspend fun getTask(taskId: Int) = task.getTask(taskId)

    private suspend fun getSourceName(code: String, type: String) =
        catalog.getSourceByCode(code, type)

    fun getOperatorsList() {
        viewModelScope.launch {
            operators.value = catalog.getOperatorsList()
        }
    }

    private fun getCheckedConditions() =
        customer.getCheckedConditions(taskId, _customerIndex.value)

    fun setStatusSpinnerPosition(position: Int) {
        if (position != _statusSpinnerPosition.value) {
            viewModelScope.launch {
                updateSourceList(position)
            }
        }
        _statusSpinnerPosition.value = position
    }

    fun setSourceSpinnerPosition(position: Int) {
        sourceSpinnerPosition.value = position
        getSelectedSourceCode(position)
    }

    private fun getSelectedSourceCode(position: Int) {
        selectedSourceCode = if (position != 0) {
            sourceList?.get(position)?.code!!
        } else ""
    }

    fun setSelectedCustomer(index: Int) {
        _customerIndex.value = index
    }

    fun setSelectedBlock(blockName: String) {
        _selectedBlock.value = blockName
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

    fun selectCustomer(next: Boolean) {
        viewModelScope.launch {
            index = if (next) {
                if (index != taskCustomerQuantity) {
                    index.plus(1)
                } else {
                    1
                }
            } else {
                if (index != 1) {
                    index.minus(1)
                } else {
                    taskCustomerQuantity
                }
            }
            setSelectedCustomer(index)
            setStartEditTime()
        }
    }

    fun setStartEditTime() {
        startEditTime = dateAndTimeFormat.format(Date())
    }

    fun setResultSavedState(isSaved: Boolean) {
        isResultSaved = isSaved
    }

    fun isResultSaved(): Boolean {
        return isResultSaved
    }

    companion object {

        private suspend fun getCustomerBasicInfo(
            userInfoViewModel: UserInfoViewModel,
            icons: List<Icons>
        ): BasicInfo {
            val basicInfoFieldsList = ArrayList<String>()
            val basicFields = userInfoViewModel.directory.getBasicFields(userInfoViewModel.taskId)
            basicInfoFieldsList.addAll(basicFields)
            basicInfoFieldsList.add("Counter_numb")
            val tdHash = userInfoViewModel.getTextFieldsByBlockName(basicInfoFieldsList)
            var pillar = ""
            val otherInfo = StringBuilder()
            tdHash.forEach { (key, value) ->
                if (key.isNotEmpty()) {
                    when (key) {
                        "О/р" -> {
                            userInfoViewModel.personalAccountKey = key
                            userInfoViewModel.personalAccount = value
                        }
                        "icons_account" -> {
                            val text = getNeededEmojis(icons, value)
                            userInfoViewModel.personalAccountEmoji =
                                "${userInfoViewModel.personalAccount} $text"
                        }
                        "Адреса" -> {
                            userInfoViewModel.address = value
                        }
                        "ПІБ" -> {
                            userInfoViewModel.name = value
                        }
                        "Опора" -> {
                            pillar = "Оп.$value"
                        }
                        "№ ліч." -> {
                            userInfoViewModel.counterValue = value
                            userInfoViewModel.counterEmoji =
                                "${userInfoViewModel.counterValue} ${userInfoViewModel.counterKey}"
                        }
                        "icons_counter" -> {
                            userInfoViewModel.counterKey = getNeededEmojis(icons, value)
                        }
                        else -> {
                            if (value.isNotEmpty())
                                otherInfo.append("$value ")
                        }
                    }
                }
            }
            otherInfo.append(pillar).toString()
            return BasicInfo(
                personalAccount = userInfoViewModel.personalAccountEmoji,
                address = userInfoViewModel.address,
                name = userInfoViewModel.name,
                counter = userInfoViewModel.counterEmoji,
                other = otherInfo.toString()
            )
        }

    }

}
