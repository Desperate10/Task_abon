package ua.POE.Task_abon.presentation.userinfo

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
import ua.POE.Task_abon.data.entities.TaskEntity
import ua.POE.Task_abon.data.entities.Timing
import ua.POE.Task_abon.data.mapper.mapCatalogEntityToCatalog
import ua.POE.Task_abon.data.mapper.mapResultToSavedData
import ua.POE.Task_abon.data.mapper.toTaskInfo
import ua.POE.Task_abon.domain.model.*
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
        savedStateHandle.get<Int>("id") ?: throw NullPointerException("Customer index is null")
    private val taskCustomerQuantity =
        savedStateHandle.get<Int>("count")
            ?: throw NullPointerException("Customer's quantity is null ")

    private val operators = MutableStateFlow<List<String>>(emptyList())

    private val _statusSpinnerPosition = MutableStateFlow(0)
    val statusSpinnerPosition: StateFlow<Int> = _statusSpinnerPosition

    private val sourceSpinnerPosition = MutableStateFlow(0)

    private val _taskInfo = MutableStateFlow<TaskInfo>(TaskInfo())
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

    val selectedBlockData = combine(_customerIndex, _selectedBlock) { _, selectedBlock ->
        if (selectedBlock == "Результати") {
            updateTechInfoMap()
            _techInfo.value
        } else {
            val fields = directory.getFieldsByBlockName(selectedBlock, taskId)
            getTextFieldsByBlockName(fields)
        }
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    val savedData = combine(_customerIndex, _statusSpinnerPosition) { index, status ->
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
            catalog.getSelectedSourceName(savedData.source, type)
        } else {
            ""
        }

        return savedData.copy(source = sourceName)
    }

    private suspend fun updateSourceList(status: Int) {
        sourceList = if (status == 0) {
            catalog.getSourceList("2")
        } else {
            catalog.getSourceList("3")
        }
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
            getCustomerBasicInfo(this, icons)
        }.filter { it.name.isNotEmpty() }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private fun getTextFieldsByBlockName(fields: List<String>) =
        customer.getFieldsByBlock(taskId, fields, _customerIndex.value)

    private suspend fun updateTechInfoMap() {
        val fields = directory.getFieldsByBlockName("Тех.информация", taskId)
        _techInfo.value =
            customer.getTextByFields("TD$taskId", fields, _customerIndex.value)
    }

    //save date when pressing saveResult
    private suspend fun saveEditTiming(firstEditDate: String, date: String) {
        if (timing.getStartTaskDate(taskId, _customerIndex.value).isNullOrEmpty()) {
            timing.insertTiming(
                Timing(
                    taskId = taskId,
                    num = _customerIndex.value,
                    startTaskTime = firstEditDate,
                    endTaskTime = date
                )
            )
        } else if (timing.getFirstEditDate(taskId, _customerIndex.value).isNullOrEmpty()) {
            timing.updateFirstEditDate(
                taskId,
                _customerIndex.value,
                firstEditDate
            )
            updateEditTime(taskId, date)
        } else {
            updateEditTime(taskId, date)
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
        date: String,
        zone1: String,
        zone2: String,
        zone3: String,
        note: String,
        phoneNumber: String,
        isMainPhone: Boolean,
        lat: String,
        lng: String,
        photo: String,
        selectCustomer: Boolean,
        isNext: Boolean
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (phoneNumber.isNotEmpty() && (phoneNumber.take(3) !in operators.value || phoneNumber.length < 10)) {
                    _saveAnswer.value = "Неправильний формат номера телефону"
                } else if (statusSpinnerPosition.value == 1 && sourceSpinnerPosition.value == 0) {
                    _saveAnswer.value = "Ви забули вказати джерело"
                } else if (zone1.isNotEmpty() || (statusSpinnerPosition.value == 1 && sourceSpinnerPosition.value != 0)) {
                    val currentDateAndTime =
                        dateAndTimeFormat.format(Date())
                    saveEditTiming(startEditTime, currentDateAndTime)
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
                    val user =
                        customer.getTextByFields("TD$taskId", fields, customerIndex.value)
                    val isMainPhoneInt = if (isMainPhone) 1 else 0
                    val photoValid = if (photo.length > 4) {
                        photo
                    } else {
                        null
                    }

                    val saveData = Result(
                        _taskInfo.value.name,
                        _taskInfo.value.date,
                        taskId,
                        _taskInfo.value.filial,
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
                        preloadResultTab.value.type,
                        counter,
                        preloadResultTab.value.zoneCount,
                        preloadResultTab.value.capacity,
                        preloadResultTab.value.averageUsage,
                        lat,
                        lng,
                        personalAccount,
                        user["family"],
                        user["Adress"],
                        photoValid,
                        user["counpleas"]
                    )

                    result.insertNewData(saveData)
                    customer.setDone(taskId, user["num"]!!)
                    setResultSavedState(true)
                    if (selectCustomer) {
                        selectCustomer(isNext)
                    }
                    _saveAnswer.value = "Результати збережено"
                } else {
                    _saveAnswer.value = "Ви не ввели нові показники"
                }
            }
        }
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
                        "icons_account", "Иконки л/с" -> {
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
                        "icons_counter", "Иконки с" -> {
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
