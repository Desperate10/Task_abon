package ua.POE.Task_abon.presentation.ui.userinfo

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidbuts.multispinnerfilter.KeyPairBoolData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.POE.Task_abon.data.dao.*
import ua.POE.Task_abon.data.entities.TimingEntity
import ua.POE.Task_abon.data.mapper.ResultMapper
import ua.POE.Task_abon.data.mapper.mapCatalogEntityToCatalog
import ua.POE.Task_abon.data.mapper.mapResultToSavedData
import ua.POE.Task_abon.data.mapper.toTaskInfo
import ua.POE.Task_abon.presentation.model.*
import ua.POE.Task_abon.utils.getNeededEmojis
import ua.POE.Task_abon.utils.mapLatestIterable
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val icons: List<Icons>,
    savedStateHandle: SavedStateHandle,
    private val directory: DirectoryDao,
    private val task: TaskDao,
    private val customer: TaskDataDaoImpl,
    private val timing: TimingDao,
    private val result: ResultDao,
    private val catalog: CatalogDao
) : ViewModel() {

    private var sourceType: String = "2"
    private var startEditTime: String = ""
    private var personalAccount = ""
    private var personalAccountKey = ""
    private var personalAccountEmoji = ""
    private var address = ""
    private var name = ""
    private var counterKey = ""
    private var counterPlace = ""
    private var phoneNumber = ""
    private var counterValue = ""
    private var identificationCode = ""
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

    //тоже нужно отслеживать во фрагменте
    private val _sourceSpinnerPosition = MutableStateFlow(0)
    val sourceSpinnerPosition: StateFlow<Int> = _sourceSpinnerPosition

    private val _sourceSpinnerPositionCode = MutableStateFlow("")
    val sourceSpinnerPositionCode: StateFlow<String> = _sourceSpinnerPositionCode

    private val _selectedFeatureList = MutableStateFlow<List<String>>(emptyList())
    val selectedFeatureList: StateFlow<List<String>> = _selectedFeatureList

    //нужно ли здесь сохранение стейта?
    private val _taskInfo = MutableStateFlow(Task())
    private val _techInfo = MutableStateFlow<Map<String, String>>(emptyMap())

    private val _customerIndex = MutableStateFlow(index)
    val customerIndex: StateFlow<Int> = _customerIndex

    private val _selectedBlock = MutableStateFlow(RESULTS_BLOCK)
    val selectedBlock: StateFlow<String> = _selectedBlock

    private val _saveAnswer = MutableSharedFlow<String>(0)
    val saveAnswer: SharedFlow<String> = _saveAnswer

    private val _sources = MutableStateFlow<List<String>>(emptyList())
    val sources: StateFlow<List<String>> = _sources

    private val _isResultSaved = MutableStateFlow(false)

    private val timer = Timer()
    private var time = 0

    init {
        startTimer()
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }

    //Tracking controllers time management in seconds
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

    //store main spinner block names
    val blockNames = flow {
        val blockNameList = mutableListOf<String>()
        blockNameList.add(0, RESULTS_BLOCK)
        emit(blockNameList)
        blockNameList.addAll(directory.getBlockNames())
        emit(blockNameList)
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), listOf(RESULTS_BLOCK))

    //store selected block name data
    val selectedBlockData = combine(_customerIndex, _selectedBlock) { _, selectedBlock ->
        if (selectedBlock == RESULTS_BLOCK) {
            updateTechInfoMap()
            _techInfo.value
        } else {
            try {
                val fields = directory.getFieldsByBlockName(selectedBlock, taskId)
                customer.getFieldsByBlock(taskId, fields, _customerIndex.value)
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    //getting savedData of current customer
    val savedData = combine(_customerIndex, _statusSpinnerPosition) { index, _ ->
        getSavedData(index)
    }
        .debounce(50L)
        .flowOn(Dispatchers.IO)
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 2)

    private suspend fun getSavedData(index: Int): SavedData {
        updateSourceList()
        val savedData = mapResultToSavedData(result.getResultByCustomer(taskId, index))
        val sourceName = if (!savedData.source.isNullOrEmpty()) {
            catalog.getSelectedSourceName(savedData.source, sourceType)
        } else {
            ""
        }
        return savedData.copy(source = sourceName)
    }

    //updating list of sources in source spinner(depends on statusSpinner)
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

    //store data of tech block for selected customer
    @OptIn(ExperimentalCoroutinesApi::class)
    val preloadResultTab: StateFlow<Technical> = _customerIndex
        .flatMapLatest {
            selectedBlock
        }
        .mapLatest {
            updateTechInfoMap()
        }
        .mapLatest { showTechInfo() }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), Technical())

    private fun showTechInfo(): Technical {
        val controlInfo = StringBuilder()
        val technical = Technical()
        _techInfo.value.forEach { (key, value) ->
            when (key) {
                "TimeZonalId" -> {
                    technical.zoneCount = value
                }
                "Lastdate" -> {
                    technical.lastDate = value
                }
                "Lastlcount" -> {
                    technical.lastCount = value
                }
                "srnach" -> {
                    technical.averageUsage = value
                }
                "type" -> {
                    technical.type = value
                }
                "Counter_numb" -> {
                    technical.counter = value
                }
                "Rozr" -> {
                    technical.capacity = value
                }
                "contr_date" -> {
                    technical.checkDate = value
                    controlInfo.append(" $value")
                }
                "contr_pok" -> {
                    controlInfo.append(" $value")
                }
                "contr_name" -> {
                    controlInfo.append(" $value")
                }
                "source_inf" -> {
                    controlInfo.append(" $value")
                }
            }
        }
        technical.inspector = controlInfo.toString()
        return technical
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
        basicInfoFieldsList.add("Ident_code")
        basicInfoFieldsList.add("counpleas")
        basicInfoFieldsList.add("tel")
        val tdHash = customer.getFieldsByBlock(taskId, basicInfoFieldsList, _customerIndex.value)
        var pillar = ""
        val otherInfo = StringBuilder()
        tdHash.forEach { (key, value) ->
            if (key.isNotEmpty()) {
                //Log.d("testimkey", key)
                when (key) {
                    "О/р" -> {
                        //Log.d("testimA", personalAccount.toString())
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
                    "ІД код" -> {
                        identificationCode = value
                    }
                    "Телефон" -> {
                        phoneNumber = value
                    }
                    "Місце вст.ліч." -> {
                        counterPlace = value
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
            identificationCode = identificationCode,
            counterPlace = counterPlace,
            other = otherInfo.toString(),
            phoneNumber = phoneNumber
        )
    }

    private suspend fun updateTechInfoMap() {
        val fields = directory.getFieldsByBlockName(TECHNICAL_BLOCK, taskId)
        _techInfo.value =
            customer.getFieldsValue("TD$taskId", fields, _customerIndex.value)
    }

    //save date timings when pressing saveResult
    private suspend fun saveEditTiming() {
        val currentDateAndTime =
            dateAndTimeFormat.format(Date())
        //when saving first time save only start and end date
        if (timing.getStartTaskDate(taskId, _customerIndex.value).isNullOrEmpty()) {
            timing.insertTiming(
                TimingEntity(
                    taskId = taskId,
                    num = _customerIndex.value,
                    startTaskTime = startEditTime,
                    endTaskTime = currentDateAndTime
                )
            )
            //when editing first time save edit time and seconds spent for editing
        } else if (timing.getFirstEditDate(taskId, _customerIndex.value).isNullOrEmpty()) {
            timing.updateFirstEditDate(
                taskId,
                _customerIndex.value,
                startEditTime
            )
            updateEditTime(taskId, currentDateAndTime)
            //else update previous edit data
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

    //Save data that came from controller and getting ready for creating xml
    // storing data in result table
    fun saveResults(newData: DataToSave) {
        viewModelScope.launch {
            delay(300)
            if (isNewDataValid(newData)) {
                withContext(Dispatchers.IO) {
                    saveEditTiming()
                    saveData(newData)
                    setResultSavedState(true)
                    if (newData.selectCustomer) {
                        selectCustomer(newData.isNext)
                    }
                    _saveAnswer.emit("Результати збережено")
                }
            }
        }
    }

    private suspend fun isNewDataValid(data: DataToSave): Boolean {
        var isValid = false
        if (data.phoneNumber.isNotEmpty() && (data.phoneNumber.take(3) !in operators.value || data.phoneNumber.length < 10)) {
            _saveAnswer.emit("Неправильний формат номера телефону")
        } else if (data.identificationCode.isNotEmpty() && data.identificationCode.length < 10) {
            _saveAnswer.emit("Неправильний формат IД-кода")
        } else if (data.status == 1 && _sourceSpinnerPosition.value == 0) {
            _saveAnswer.emit("Ви забули вказати джерело")
        } else if (data.zone1.isNotEmpty() || (data.status == 1 && _sourceSpinnerPosition.value != 0)) {
            isValid = true
        } else {
            _saveAnswer.emit("Ви не ввели нові показники")
        }
        return isValid
    }

    private suspend fun saveData(newData: DataToSave) {
        val missingData = getCustomerMissingData()
        val saveData = ResultMapper.mapNeededDataToResult(
            _taskInfo.value,
            missingData,
            newData,
            preloadResultTab.value
        )
        changeStatusToDone(missingData["num"]!!)
        result.insertNewData(saveData)
    }

    //change customers status to done
    // uses when filtering users on taskDetailFragment
    private fun changeStatusToDone(num: String) {
        customer.setDone(taskId, num)
    }

    private fun getCustomerMissingData(): HashMap<String, String> {
        val fields =
            listOf(
                "num",
                "accountId",
                "Numbpers",
                "family",
                "Adress",
                "tel",
                "counpleas",
                "Ident_code",
                "Physical_PersonId"
            )
        return customer.getFieldsValue("TD$taskId", fields, customerIndex.value)
    }

    fun getTask() {
        viewModelScope.launch {
            _taskInfo.value = task.getTask(taskId).toTaskInfo()
        }
    }


    fun getMobileOperatorsList() {
        viewModelScope.launch {
            operators.value = catalog.getOperatorsList()
        }
    }

    private fun getCheckedConditions() =
        customer.getCustomerPointCondition(taskId, _customerIndex.value)

    fun setStatusSpinnerPosition(position: Int) {
        handleStatusChange(position)
        _statusSpinnerPosition.value = position
    }

    fun handleStatusChange(position: Int) {
        sourceType = (position + 2).toString()
        if (position != _statusSpinnerPosition.value) {
            viewModelScope.launch {
                updateSourceList()
            }
        }
    }

    fun setSourceSpinnerPosition(position: Int) {
        _sourceSpinnerPosition.value = position
        getSelectedSourceCode(position)
    }

    private fun getSelectedSourceCode(position: Int) {
        _sourceSpinnerPositionCode.value = if (position != 0) {
            sourceList?.get(position)?.code!!
        } else ""
    }

    fun setSelectedCustomer(index: Int) {
        _customerIndex.value = index
    }

    fun setSelectedBlock(blockName: String) {
        _selectedBlock.value = blockName
    }

    //resetting timer when changing customer
    private fun resetTimer() {
        time = 0
    }

    fun setItems(items: List<KeyPairBoolData>) {
        _selectedFeatureList.value = items.flatMap { item ->
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
            resetTimer()
            setSelectedCustomer(index)
            setStartEditTime()
        }
    }

    fun setStartEditTime() {
        startEditTime = dateAndTimeFormat.format(Date())
    }

    fun setResultSavedState(isSaved: Boolean) {
        _isResultSaved.value = isSaved
    }

    fun isResultSaved(): Boolean {
        return _isResultSaved.value
    }

    fun deletePhoto(uri: Uri?) {
        viewModelScope.launch {
            uri?.path?.let { File(it).delete() }
            withContext(Dispatchers.IO) {
                result.deletePhoto(taskId, _customerIndex.value)
            }
        }
    }

    companion object {
        private const val RESULTS_BLOCK = "Результати"
        private const val TECHNICAL_BLOCK = "Тех.інформація"
    }

}
