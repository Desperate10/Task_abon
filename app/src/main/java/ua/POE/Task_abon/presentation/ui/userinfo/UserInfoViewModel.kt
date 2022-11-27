package ua.POE.Task_abon.presentation.ui.userinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidbuts.multispinnerfilter.KeyPairBoolData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.POE.Task_abon.data.dao.*
import ua.POE.Task_abon.data.dao.impl.TaskCustomerDaoImpl
import ua.POE.Task_abon.data.entities.Result
import ua.POE.Task_abon.data.entities.Timing
import ua.POE.Task_abon.data.mapper.mapCatalogEntityToCatalog
import ua.POE.Task_abon.data.mapper.mapResultToSavedData
import ua.POE.Task_abon.data.mapper.toTaskInfo
import ua.POE.Task_abon.presentation.model.*
import ua.POE.Task_abon.utils.getNeededEmojis
import ua.POE.Task_abon.utils.mapLatestIterable
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val icons: List<Icons>,
    savedStateHandle: SavedStateHandle,
    private val directory: DirectoryDao,
    private val task: TaskDao,
    private val customer: TaskCustomerDaoImpl,
    private val timing: TimingDao,
    private val result: ResultDao,
    private val catalog: CatalogDao
) : ViewModel() {

    private var sourceType: String = "2"
    private var isResultSaved = false
    private var startEditTime: String = ""
    private var selectedSourceCode = ""
    private var multiSpinnerSelectedFeatures = listOf<String>()
    private var counter = ""
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
        savedStateHandle.get<Int>("userId") ?: throw NullPointerException("Customer index is null")
    private val taskCustomerQuantity =
        savedStateHandle.get<Int>("count")
            ?: throw NullPointerException("Customer's quantity is null ")

    private val operators = MutableStateFlow<List<String>>(emptyList())

    private val _statusSpinnerPosition = MutableStateFlow(0)
    val statusSpinnerPosition: StateFlow<Int> = _statusSpinnerPosition

    private val sourceSpinnerPosition = MutableStateFlow(0)

    private val _taskInfo = MutableStateFlow(TaskInfo())
    private val _techInfo = MutableStateFlow<Map<String, String>>(emptyMap())

    private val _customerIndex = MutableStateFlow(index)
    val customerIndex: StateFlow<Int> = _customerIndex

    private val _selectedBlock = MutableStateFlow(RESULTS_BLOCK)
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
        blockNameList.add(0, RESULTS_BLOCK)
        emit(blockNameList)
        blockNameList.addAll(directory.getBlockNames())
        emit(blockNameList)
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), listOf(RESULTS_BLOCK))

    val selectedBlockData = combine(_customerIndex, _selectedBlock) { _, selectedBlock ->
        if (selectedBlock == RESULTS_BLOCK) {
            updateTechInfoMap()
            _techInfo.value
        } else {
            val fields = directory.getFieldsByBlockName(selectedBlock, taskId)
            customer.getFieldsByBlock(taskId, fields, _customerIndex.value)
        }
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    val savedData = combine(_customerIndex, _statusSpinnerPosition) { index, _ ->
        getSavedData(index)
    }
        .flowOn(Dispatchers.Main)
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 2)

    //разобраться с сохранением состояния
    private suspend fun getSavedData(index: Int): SavedData {
        val savedData = mapResultToSavedData(result.getResultByCustomer(taskId, index))
        updateSourceList()
        val sourceName = if (!savedData.source.isNullOrEmpty()) {
            catalog.getSelectedSourceName(savedData.source, sourceType)
        } else {
            ""
        }

        return savedData.copy(source = sourceName)
    }

    private suspend fun updateSourceList() {
        sourceList = catalog.getSourceList(sourceType)
            .map { mapCatalogEntityToCatalog(it) }
        _sources.value = sourceList!!.map { it.text.toString() }
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
    val preloadResultTab: StateFlow<TechInfo> = _customerIndex
        .flatMapLatest {
            selectedBlock
        }
        .mapLatest {
            updateTechInfoMap()
        }
        .mapLatest { showTechInfo() }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), TechInfo())

    private fun showTechInfo(): TechInfo {
        val controlInfo = StringBuilder()
        val techInfo = TechInfo()
        _techInfo.value.forEach { (key, value) ->
            when (key) {
                "TimeZonalId" -> {
                    techInfo.zoneCount = value
                }
                "Lastdate" -> {
                    techInfo.lastDate = value
                }
                "Lastlcount" -> {
                    techInfo.lastCount = value
                }
                "srnach" -> {
                    techInfo.averageUsage = value
                }
                "type" -> {
                    techInfo.type = value
                }
                "Counter_numb" -> {
                    counter = value
                }
                "Rozr" -> {
                    techInfo.capacity = value
                }
                "contr_date" -> {
                    techInfo.checkDate = value
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
        techInfo.inspector = controlInfo.toString()
        return techInfo
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val basicInfo = _customerIndex
        .mapLatest {
            getCustomerBasicInfo(icons)
        }
        .filter { it.name.isNotEmpty() }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)


    private suspend fun getCustomerBasicInfo(
        icons: List<Icons>
    ): BasicInfo {
        val basicInfoFieldsList = ArrayList<String>()
        val basicFields = directory.getBasicFields(taskId)
        basicInfoFieldsList.addAll(basicFields)
        basicInfoFieldsList.add("Counter_numb")
        val tdHash = customer.getFieldsByBlock(taskId, basicInfoFieldsList, _customerIndex.value)
        var pillar = ""
        val otherInfo = StringBuilder()
        tdHash.forEach { (key, value) ->
            if (key.isNotEmpty()) {
                when (key) {
                    "О/р" -> {
                        personalAccountKey = key
                        personalAccount = value
                    }
                    "icons_account", "Иконки л/с" -> {
                        val text = getNeededEmojis(icons, value)
                        personalAccountEmoji =
                            "$personalAccount $text"
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
                        counterEmoji =
                            "$counterValue $counterKey"
                    }
                    "icons_counter", "Иконки с" -> {
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
        return BasicInfo(
            personalAccount = personalAccountEmoji,
            address = address,
            name = name,
            counter = counterEmoji,
            other = otherInfo.toString()
        )
    }

    private suspend fun updateTechInfoMap() {
        val fields = directory.getFieldsByBlockName(TECHNICAL_BLOCK, taskId)
        _techInfo.value =
            customer.getTextByFields("TD$taskId", fields, _customerIndex.value)
    }

    //save date when pressing saveResult
    private suspend fun saveEditTiming() {
        val currentDateAndTime =
            dateAndTimeFormat.format(Date())
        if (timing.getStartTaskDate(taskId, _customerIndex.value).isNullOrEmpty()) {
            timing.insertTiming(
                Timing(
                    taskId = taskId,
                    num = _customerIndex.value,
                    startTaskTime = startEditTime,
                    endTaskTime = currentDateAndTime
                )
            )
        } else if (timing.getFirstEditDate(taskId, _customerIndex.value).isNullOrEmpty()) {
            timing.updateFirstEditDate(
                taskId,
                _customerIndex.value,
                startEditTime
            )
            updateEditTime(taskId, currentDateAndTime)
        } else {
            updateEditTime(taskId, currentDateAndTime)
        }
        resetTimer()
    }

    //adding +1 to edit count
    private suspend fun updateEditTime(taskId: Int, date: String) {
        saveEditTime(taskId, _customerIndex.value, time)
        timing.upEditCount(taskId, _customerIndex.value)
        timing.updateLastEditDate(taskId, _customerIndex.value, date)
    }

    private suspend fun saveEditTime(taskId: Int, num: Int, time: Int) {
        val seconds: Int = timing.getEditTime(taskId, num) ?: 0
        val newEditTime = seconds + time
        timing.updateEditSeconds(taskId, num, newEditTime)
    }

    fun saveResults(
        newData: DataToSave
    ) {
        if (isNewDataValid(newData)) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    saveEditTiming()
                    val currentCustomer = getCustomerMissingData()
                    val isMainPhoneInt = if (newData.isMainPhone) 1 else 0
                    val photoValid = if (newData.photo.length > 4) {
                        newData.photo
                    } else {
                        null
                    }

                    val saveData = Result(
                        _taskInfo.value.name,
                        _taskInfo.value.date,
                        taskId,
                        _taskInfo.value.filial,
                        _customerIndex.value,
                        currentCustomer["num"]!!,
                        currentCustomer["accountId"]!!,
                        newData.date,
                        statusSpinnerPosition.value.toString(),
                        selectedSourceCode,
                        multiSpinnerSelectedFeatures.joinToString(),
                        newData.zone1,
                        newData.zone2,
                        newData.zone3,
                        newData.note,
                        currentCustomer["tel"]!!,
                        newData.phoneNumber,
                        isMainPhoneInt,
                        "",
                        preloadResultTab.value.type,
                        counter,
                        preloadResultTab.value.zoneCount,
                        preloadResultTab.value.capacity,
                        preloadResultTab.value.averageUsage,
                        newData.lat,
                        newData.lng,
                        personalAccount,
                        currentCustomer["family"],
                        currentCustomer["Adress"],
                        photoValid,
                        currentCustomer["counpleas"]
                    )

                    result.insertNewData(saveData)
                    customer.setDone(taskId, currentCustomer["num"]!!)
                    setResultSavedState(true)
                    if (newData.selectCustomer) {
                        selectCustomer(newData.isNext)
                    }
                    _saveAnswer.value = "Результати збережено"
                }
            }
        }
    }

    private fun isNewDataValid(data: DataToSave): Boolean {
        var isValid = false
        if (data.phoneNumber.isNotEmpty() && (data.phoneNumber.take(3) !in operators.value || data.phoneNumber.length < 10)) {
            _saveAnswer.value = "Неправильний формат номера телефону"
        } else if (statusSpinnerPosition.value == 1 && sourceSpinnerPosition.value == 0) {
            _saveAnswer.value = "Ви забули вказати джерело"
        } else if (data.zone1.isNotEmpty() || (statusSpinnerPosition.value == 1 && sourceSpinnerPosition.value != 0)) {
            isValid = true
        } else {
            _saveAnswer.value = "Ви не ввели нові показники"
        }
        return isValid
    }

    private fun getCustomerMissingData(): HashMap<String,String> {
        val fields =
            listOf(
                "num",
                "accountId",
                "Numbpers",
                "family",
                "Adress",
                "tel",
                "counpleas"
            )
        return customer.getTextByFields("TD$taskId", fields, customerIndex.value)
    }

    fun getTask() {
        viewModelScope.launch {
            _taskInfo.value = task.getTask(taskId).toTaskInfo()
        }
    }

    fun getOperatorsList() {
        viewModelScope.launch {
            operators.value = catalog.getOperatorsList()
        }
    }

    private fun getCheckedConditions() =
        customer.getCheckedConditions(taskId, _customerIndex.value)

    fun setStatusSpinnerPosition(position: Int) {
        sourceType = (position + 2).toString()
        if (position != _statusSpinnerPosition.value) {
            viewModelScope.launch {
                updateSourceList()
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
        private const val RESULTS_BLOCK = "Результати"
        private const val TECHNICAL_BLOCK = "Тех.информация"
    }

}
