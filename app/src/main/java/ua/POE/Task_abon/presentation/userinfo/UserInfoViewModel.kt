package ua.POE.Task_abon.presentation.userinfo

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidbuts.multispinnerfilter.KeyPairBoolData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.POE.Task_abon.data.dao.CatalogDao
import ua.POE.Task_abon.data.dao.ResultDao
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
import java.text.SimpleDateFormat
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
    private var sourceList = listOf<Catalog>()
    private val operators by lazy(LazyThreadSafetyMode.NONE) { getOperatorsList() }
    private val dateAndTime = "dd.MM.yyyy HH:mm:ss"
    private val dateAndTimeformat = SimpleDateFormat(dateAndTime, Locale.getDefault())

    private val taskId =
        savedStateHandle.get<Int>("taskId") ?: throw RuntimeException("taskId is null")
    private var index =
        savedStateHandle.get<Int>("id") ?: throw RuntimeException("Customer index is null")
    private val taskCustomerQuantity =
        savedStateHandle.get<Int>("count") ?: throw RuntimeException("Customer's quantity is null ")

    private val _statusSpinnerPosition = MutableStateFlow(0)
    val statusSpinnerPosition: StateFlow<Int> = _statusSpinnerPosition

    private val sourceSpinnerPosition = MutableStateFlow(0)

    private val statusDoneSourceList = MutableStateFlow(listOf("-Не вибрано-"))
    private val statusNotDoneSourceList = MutableStateFlow(listOf("-Не вибрано-"))

    private fun getStatusDoneSourceList() {
        viewModelScope.launch {
            statusDoneSourceList.value = catalogDao.getSourceList("2")
                .map { mapCatalogEntityToCatalog(it) }
                .map { it.text.toString() }
        }
    }

    private fun getStatusNotDoneSourceList() {
        viewModelScope.launch {
            statusDoneSourceList.value = catalogDao.getSourceList("3")
                .map { mapCatalogEntityToCatalog(it) }
                .map { it.text.toString() }
        }
    }

    private val _customerIndex = MutableStateFlow(index)
    val customerIndex: StateFlow<Int> = _customerIndex

    private val _selectedBlock = MutableStateFlow("Результати")
    val selectedBlock: StateFlow<String> = _selectedBlock

    val blockNames = flow {
        val blockNameList = mutableListOf<String>()
        blockNameList.add(0, "Результати")
        emit(blockNameList)
        blockNameList.addAll(directoryRepository.getBlockNames())
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
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 2)

    /* private fun getSavedData(index: Int) = flow {
         val savedData = mapResultToSavedData(resultDao.getResultUser(taskId, index))
         Log.d("testim", sourceList.toString())
         val spinnerPosition = if (!savedData.source.isNullOrEmpty()) {
             sourceList.map { it.code }.indexOf(savedData.source)
         } else {
             0
         }

         emit(savedData.copy(source = spinnerPosition.toString()))
     }*/

    private fun getSavedData(index: Int): Flow<SavedData?> {
        return resultDao.getResultUser(taskId, index).map {
            mapResultToSavedData(it)
        }
    }

    private val timer = Timer()
    var time = 0

    init {
        startTimer()
        setStartEditTime()
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
        val sourceTextList = mutableListOf<String>()
        sourceTextList.add("-Не вибрано-")
        emit(sourceTextList)
        sourceList = if (position == 0) {
            catalogDao.getSourceList("2")
        } else {
            catalogDao.getSourceList("3")
        }.map { mapCatalogEntityToCatalog(it) }
        sourceTextList.addAll(sourceList.map { it.text.toString() })
        emit(sourceTextList)
    }

    val sources: SharedFlow<List<String>> = statusSpinnerPosition
        .flatMapConcat { getSourceListFlow(it) }
        //.flowOn(Dispatchers.IO)
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 2)


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
        .mapLatest {
            getTechInfo()
        }

    private fun getTechInfo(): TechInfo {
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

    private fun getFieldsByBlockName(name: String): List<String> =
        directoryRepository.getFieldsByBlockName(name, taskId)

    private fun getTextFieldsByBlockName(fields: List<String>) =
        testEntityRepository.getFieldsByBlock(taskId, fields, _customerIndex.value)

    private fun getTechInfoTextByFields(): HashMap<String, String> {
        val fields = getFieldsByBlockName("Тех.информация")
        return testEntityRepository.getTextByFields("TD$taskId", fields, _customerIndex.value)
    }

    //save date when pressing saveResult
    private fun saveEditTiming(firstEditDate: String, date: String) {
        viewModelScope.launch {
            coroutineScope {
                if (timingRepository.isStartTaskDateEmpty(taskId, _customerIndex.value)) {
                    timingRepository.insertTiming(
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
                } else if (timingRepository.isFirstEditDateEmpty(taskId, _customerIndex.value)) {
                    timingRepository.updateFirstEditDate(
                        taskId,
                        _customerIndex.value,
                        firstEditDate
                    )
                    timingRepository.updateEditCount(taskId, _customerIndex.value, 1)
                    saveEndEditDate(taskId, _customerIndex.value, date)
                    saveEditTime(taskId, _customerIndex.value, time)
                } else {
                    //adding +1 to edit count
                    saveEditTime(taskId, _customerIndex.value, time)
                    saveEndEditDate(taskId, _customerIndex.value, date)
                    timingRepository.upEditCount(taskId, _customerIndex.value)

                }
            }
        }
    }

    //save date when finish editing task when onStop() userInfoFragment
    private fun saveEndEditDate(taskId: Int, num: Int, date: String) {
        viewModelScope.launch {
            if (!timingRepository.isFirstEditDateEmpty(taskId, num)) {
                timingRepository.updateLastEditDate(taskId, num, date)
            }
        }
    }

    private fun saveEditTime(taskId: Int, num: Int, time: Int) {
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
        isMainPhone: Boolean,
        lat: String,
        lng: String,
        photo: String
    ) {
        viewModelScope.launch {
            Log.d("testim", customerIndex.value.toString())
            if (phoneNumber.isNotEmpty() && (phoneNumber.take(3) !in operators || phoneNumber.length < 10)) {
                Log.d("testim", "Неправильний формат номера телефону")
            } else if (statusSpinnerPosition.value == 1 && sourceSpinnerPosition.value == 0) {
                Log.d("testim", "Ви забули вказати джерело")
            } else if (zone1.isNotEmpty() || (statusSpinnerPosition.value == 1 && sourceSpinnerPosition.value != 0)) {
                resetTimer()
                val task: TaskEntity = getTask(taskId)
                val fields =
                    listOf("num", "accountId", "Numbpers", "family", "Adress", "tel", "counpleas")
                val user =
                    testEntityRepository.getTextByFields("TD$taskId", fields, customerIndex.value)
                val isMainPhoneInt = if (isMainPhone) 1 else 0
                val photoValid = if (photo.length > 4) {
                    photo
                } else {
                    null
                }

                val result = Result(
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
                    dateAndTime.format(Date())
                saveEditTiming(startEditTime, currentDateAndTime)
                resultDao.insertNewData(result)
                Log.d("testim", "zashlo2")
                testEntityRepository.setDone(taskId, user["num"]!!)
                setResultSavedState(true)
                Log.d("testim", "Результати збережено")
            } else {
                Log.d("testim", "Ви не ввели нові показники")

            }
        }
    }

    /*fun saveResults(
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
    }*/

    private suspend fun getTask(taskId: Int) = taskRepository.getTask(taskId)

    fun getSourceName(code: String, type: String) = catalogDao.getSourceByCode(code, type)

    private fun getOperatorsList() = catalogDao.getOperatorsList()

    private fun getCheckedConditions() =
        testEntityRepository.getCheckedConditions(taskId, _customerIndex.value)

    fun setStatusSpinnerPosition(position: Int) {
        _statusSpinnerPosition.value = position
    }

    fun setSourceSpinnerPosition(position: Int) {
        sourceSpinnerPosition.value = position
        getSelectedSourceCode(position)

    }

    private fun getSelectedSourceCode(position: Int) {
        selectedSourceCode = if (position != 0) {
            sourceList[position - 1].code!!
        } else ""
    }

    fun setSelectedCustomer(index: Int) {
        _customerIndex.value = index
    }

    fun setSelectedBlock(blockName: String) {
        _selectedBlock.value = blockName
    }

    /*fun selectPreviousCustomer() {
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
    }*/

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

    private fun setStartEditTime() {
        startEditTime = dateAndTimeformat.format(Date())
    }

    fun setResultSavedState(IsSaved: Boolean) {
        isResultSaved = IsSaved
    }

    fun isResultSaved(): Boolean {
        return isResultSaved
    }

}
