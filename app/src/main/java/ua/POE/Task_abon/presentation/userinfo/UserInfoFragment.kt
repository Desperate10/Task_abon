package ua.POE.Task_abon.presentation.userinfo

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextWatcher
import android.text.util.Linkify
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.androidbuts.multispinnerfilter.KeyPairBoolData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import ua.POE.Task_abon.R
import ua.POE.Task_abon.databinding.FragmentUserInfoBinding
import ua.POE.Task_abon.domain.model.*
import ua.POE.Task_abon.presentation.MainActivity
import ua.POE.Task_abon.presentation.adapters.ImageAdapter
import ua.POE.Task_abon.utils.autoCleaned
import ua.POE.Task_abon.utils.getEmojiByUnicode
import ua.POE.Task_abon.utils.getRawTextFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class UserInfoFragment : Fragment(), AdapterView.OnItemSelectedListener, View.OnClickListener,
    DatePickerDialog.OnDateSetListener {

    private var binding: FragmentUserInfoBinding by autoCleaned()
    private val viewModel: UserInfoViewModel by viewModels()
    private var filial: String? = null
    private val calendar = Calendar.getInstance(TimeZone.getDefault())
    private val myFormat = "dd.MM.yyyy"
    private val sdformat = SimpleDateFormat(myFormat, Locale.getDefault())

    private var isFirstLoad = false
    var source = ""
    private var source2 = listOf<String>()
    private var isResultSaved = false
    private var sourceText = listOf<String>()
    var massiv2 = ArrayList<KeyPairBoolData>()
    private val operators by lazy { viewModel.getOperatorsList() }
    private val sourceAdapter: ArrayAdapter<String> by lazy {
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            sourceText
        )
    }

    lateinit var catalog: List<Catalog>
    lateinit var featureList: List<Catalog>
    lateinit var locationManager: LocationManager
    private val imageAdapter: ImageAdapter by autoCleaned {
        ImageAdapter(requireContext(), items, uri)
    }
    private val items = ArrayList<Image>()
    private val uri = ArrayList<String>()
    private var imageUri: Uri? = null
    private val icons by lazy {
        resources.getRawTextFile(R.raw.icons)
    }
    private var firstEditDate: String = ""
    private var zone1watcher: TextWatcher? = null
    private var zone2watcher: TextWatcher? = null
    private var zone3watcher: TextWatcher? = null

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getSourceList.collect {
                    //Каталог убрать во вьюмодель
                    //catalog = it
                    sourceText = it
                    sourceAdapter.notifyDataSetChanged()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sourceSpinnerPosition.collect {
                    //после каталога убрать слушатель второго спиннера на вьюмодель
                    source = if (it != 0) {
                        catalog[it - 1].code!!
                    } else ""
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getFeatureList().collect {
                    featureList = it
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.blockNames.collect {
                    setupMainBlockSpinner(it)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.customerIndex
                    .flatMapLatest {
                        viewModel.getCustomerBasicInfo(icons)
                    }
                    .filter { it.name.isNotEmpty() }
                    .collectLatest {
                        getBasicInfo(it)
                    }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch {
                viewModel.selectedBlockData.collectLatest {
                    if (viewModel.selectedBlock.value != "Результати") {
                        binding.infoTables.visibility = VISIBLE
                        binding.results.root.visibility = GONE
                        updateView(it)
                    } else {
                        binding.infoTables.visibility = GONE
                        binding.results.root.visibility = VISIBLE
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.preloadResultTab.collectLatest {
                    preloadResultTab(it)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.result.collectLatest {
                    //Data и featurelist можно обработать во viewModel и отдать сюда готовыми?
                    loadSpinnersFromResult(it)
                    resetFields()
                    it?.let { getResultIfExist(it) }
                }
            }
        }
    }

    private fun preloadResultTab(it: TechInfo) {
        when(it.zoneCount) {
            "1" -> {
                binding.results.secondZoneRow.visibility = GONE
                binding.results.thirdZoneRow.visibility = GONE
            }
            "2" -> {
                binding.results.secondZoneRow.visibility = VISIBLE
                binding.results.thirdZoneRow.visibility = GONE
            }
            "3" -> {
                binding.results.secondZoneRow.visibility = VISIBLE
                binding.results.thirdZoneRow.visibility = VISIBLE
            }
        }

        binding.results.lastDate.text = it.lastDate
        val lastCount: List<String> = it.lastCount.split("/")

        binding.results.previousMeters1.text = lastCount[0]
        if (lastCount.size == 2) {
            binding.results.previousMeters2.text = lastCount[1]
        } else if (lastCount.size == 3) {
            binding.results.previousMeters2.text = lastCount[1]
            binding.results.previousMeters3.text = lastCount[2]
        }

        binding.results.differenceText.text = "Расход\nСредн ${it.averageUsage}"
        binding.results.contrDate.text = it.checkDate
        binding.results.contrText.text =
            resources.getString(R.string.contr_pokaz) + it.inspector

        imageAdapter.deletePhoto(requireContext())
    }

    private fun registerWatchers() {
        zone1watcher = registerWatcher(
            binding.results.newMeters1,
            binding.results.difference1,
            binding.results.previousMeters1
        )
        zone2watcher = registerWatcher(
            binding.results.newMeters2,
            binding.results.difference2,
            binding.results.previousMeters2
        )
        zone3watcher = registerWatcher(
            binding.results.newMeters3,
            binding.results.difference3,
            binding.results.previousMeters3
        )
    }

    private fun removeTextWatchers() {
        binding.results.newMeters1.removeTextChangedListener(zone1watcher)
        binding.results.newMeters2.removeTextChangedListener(zone2watcher)
        binding.results.newMeters3.removeTextChangedListener(zone3watcher)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.title = "Інформація"

        if (savedInstanceState != null) {
            viewModel.setSelectedCustomer(savedInstanceState.getInt("index"))
        }

        arguments?.let {
            filial = arguments?.getString("filial")
            isFirstLoad = requireArguments().getBoolean("isFirstLoad")
        }

       // viewModel.setSelectedCustomer(index)

        firstEditDate = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())

        checkPermissions()
        setupSecondarySpinners()
        registerWatchers()
        registerClickListeners()
        setupImageAdapter()
        observeViewModel()
    }

    private fun registerClickListeners() {
        (binding.personalAccount as TextView).setOnClickListener(this)
        (binding.counter as TextView).setOnClickListener(this)
        binding.previous.setOnClickListener(this)
        binding.next.setOnClickListener(this)
        binding.results.date.setOnClickListener(this)
        binding.results.newDate.setOnClickListener(this)
    }

    private fun setupMainBlockSpinner(list: List<String>) {
        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                list
            )
        binding.blockName.adapter = adapter
        binding.blockName.onItemSelectedListener = this
        adapter.notifyDataSetChanged()
    }

    private fun setupSecondarySpinners() {
        binding.results.sourceSpinner.adapter = sourceAdapter
        binding.results.statusSpinner.onItemSelectedListener = this
        binding.results.sourceSpinner.onItemSelectedListener = this
    }

    private fun setupImageAdapter() {
        binding.results.photoLayout.adapter = imageAdapter
        //imageAdapter.addAddButton(requireContext())

        binding.results.photoLayout.setOnItemClickListener { _, _, position, _ ->
            if (position == 0) {
                if (items.size < 2) {
                    takePhoto()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Видалити фото, щоб створити нове",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun registerWatcher(
        newMeters: EditText,
        difference: TextView,
        oldMeters: TextView
    ): TextWatcher? {
        return try {
            newMeters.doAfterTextChanged {
                if (!it.isNullOrEmpty()) {
                    difference.text = (
                            it.toString().toInt() - oldMeters.text.toString()
                                .toInt()).toString()
                }
            }
        } catch (e: NumberFormatException) {
            return null
        }
    }

    override fun onStop() {
        super.onStop()
        removeTextWatchers()
    }

    private fun takePhoto() {
        val filename = filial + "_" + binding.personalAccount?.text + "_"
        val storageDirectory: File? =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        try {
            val tempImage = File.createTempFile(filename, ".jpg", storageDirectory)
            imageUri = FileProvider.getUriForFile(
                requireContext(),
                "ua.POE.Task_abon.fileprovider",
                tempImage
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            try {
                startActivityForResult(intent, 1)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "Проблема з запуском камери", Toast.LENGTH_LONG)
                    .show()
                requireActivity().finish()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun checkPermissions() {
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA
                ), 1
            )
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000,
                1f, locationListener
            )
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                onGPS()
            }
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            binding.lat.text = location.latitude.toString()
            binding.lng.text = location.longitude.toString()
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun onGPS() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Ввімкнути GPS?").setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { _: DialogInterface?, _: Int ->
                startActivity(
                    Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    )
                )
            }
            .setNegativeButton(getString(R.string.no)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(locationListener)
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000,
                1f, locationListener
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            imageUri?.let { imageAdapter.addNewPhoto(it) }
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = imageUri
            requireActivity().sendBroadcast(mediaScanIntent)
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity(), arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ), 1
                    )
                    return
                }
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    onGPS()
                }

                // 100 ->
                if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        requireContext(),
                        "Отримано дозвіл на користування камерою",
                        Toast.LENGTH_LONG
                    )
                        .show()
                } else {
                    Toast.makeText(requireContext(), "camera permission denied", Toast.LENGTH_LONG)
                        .show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun loadSpinnersFromResult(result: ua.POE.Task_abon.data.entities.Result?) {
        val data = if (result == null) {
            viewModel.getCheckedConditions()
        } else {
            result.point_condition
        }?.split(",")?.map { it.trim() }

        massiv2.clear()
        for (feature in featureList) {
            if (data != null) {
                if (feature.code.toString() in data) {
                    massiv2.add(KeyPairBoolData(feature.text!!, true))
                } else {
                    massiv2.add(KeyPairBoolData(feature.text!!, false))
                }
            }
        }

        with(binding.results.featureSpinner) {
            isSearchEnabled = false
            isShowSelectAllButton = true
            isColorSeparation = false

            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES ->
                    setBackgroundColor(Color.GRAY)
                Configuration.UI_MODE_NIGHT_NO ->
                    setBackgroundColor(Color.WHITE)
            }
            hintText = "Можливий вибір декількох пунктів:"
            setClearText("Очистити все")
            setItems(massiv2) { items ->
                //source2.clear()
                source2 = items.flatMap { item ->
                    featureList
                        .filter { item.name == it.text }
                        .map { it.code.toString() }
                }
                /*for (i in items.indices) {
                    for (feature in featureList) {
                        if (items[i].name == feature.text) {
                            feature.code?.let { source2.add(it) }
                        }
                    }
                }*/
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.user_info_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressed()
                return true
            }
            R.id.save_customer_data -> {
                if (binding.results.phone.text.isNotEmpty() && (binding.results.phone.text.take(3)
                        .toString() !in operators || binding.results.phone.text.length < 10)
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Неправильний формат номера телефону",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (binding.lat.text != "0.0") {
                    saveResult()
                } else {
                    askAboutSavingWithoutCoords()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun askAboutSavingWithoutCoords() {
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    if (binding.results.phone.text.isNotEmpty()
                        && (binding.results.phone.text.take(3).toString() !in operators
                                || binding.results.phone.text.length < 10)
                    ) {
                        Toast.makeText(
                            requireContext(),
                            "Неправильний формат номера телефону",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        saveResult()
                    }
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    dialog.dismiss()
                }
            }
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Впевнені, що хочете зберегти без координат?").setPositiveButton(
            getString(R.string.yes),
            dialogClickListener
        )
            .setNegativeButton(getString(R.string.no), dialogClickListener).show()

    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.previous -> {
                if (!isResultSaved && (binding.results.newMeters1.text.isNotEmpty() || binding.results.sourceSpinner.selectedItemPosition == 1)) {
                    showSaveOrNotDialog(false)
                } else {
                    goPrevious()
                }
            }
            R.id.next -> {
                if (!isResultSaved && (binding.results.newMeters1.text.isNotEmpty() || binding.results.sourceSpinner.selectedItemPosition == 1)) {
                    showSaveOrNotDialog(true)
                } else {
                    goNext()
                }
            }
            R.id.personal_account, R.id.counter -> {
                showIconsDialog((v as TextView).text.toString())
            }
            R.id.date, R.id.new_date -> {
                showDatePickerDialog()
            }
        }
    }

    private fun showDatePickerDialog() {
        val dialog = DatePickerDialog(
            requireContext(), this,
            calendar[Calendar.YEAR], calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
        dialog.show()
    }

    private fun showSaveOrNotDialog(next: Boolean) {
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    if (binding.results.phone.text.isNotEmpty()
                        && (binding.results.phone.text.take(3).toString() !in operators
                                || binding.results.phone.text.length < 10)
                    ) {
                        Toast.makeText(
                            requireContext(),
                            "Неправильний формат номера телефону",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        saveResult()
                        if (next) {
                            goNext()
                        } else goPrevious()
                    }
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    dialog.dismiss()
                    if (next) {
                        goNext()
                    } else goPrevious()
                }
            }
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Зберегти зміни?")
            .setPositiveButton(getString(R.string.yes), dialogClickListener)
            .setNegativeButton(getString(R.string.no), dialogClickListener).show()
    }

    private fun goPrevious() {
        viewModel.selectPreviousCustomer()
        firstEditDate = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun goNext() {
        viewModel.selectNextCustomer()
        firstEditDate = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        when (parent.id) {
            R.id.block_name -> {
                val selectedItem = parent.getItemAtPosition(position).toString()
                viewModel.setSelectedBlock(selectedItem)
            }
            R.id.status_spinner -> {
                viewModel.setStatusSpinnerPosition(position)
            }
            R.id.source_spinner -> {
                viewModel.setSourceSpinnerPosition(position)
            }
        }
    }

    private fun updateSourceSpinner(source: List<String>) {
        massiv.clear()
        massiv.add("-Не вибрано-")
        for (i in catalogList) {
            massiv.add(i.text!!)
        }
        sourceAdapter.notifyDataSetChanged()
    }

    private fun resetFields() {
        viewModel.setStatusSpinnerPosition(0)
        binding.results.date.text = sdformat.format(calendar.time)
        binding.results.newDate.text = sdformat.format(calendar.time)
        binding.results.statusSpinner.setSelection(0)
        binding.results.sourceSpinner.setSelection(0)
        binding.results.newMeters1.setText("", TextView.BufferType.EDITABLE)
        binding.results.newMeters2.setText("", TextView.BufferType.EDITABLE)
        binding.results.newMeters3.setText("", TextView.BufferType.EDITABLE)
        binding.results.checkBox.isChecked = false
        binding.results.difference1.text = ""
        binding.results.note.setText("")
        binding.results.phone.setText("")
        binding.results.checkBox.isChecked = false
        isResultSaved = false
        imageAdapter.notifyDataSetChanged()
    }

    private fun getResultIfExist(result : ua.POE.Task_abon.data.entities.Result) {
        viewModel.setStatusSpinnerPosition(result.notDone?.toInt() ?: 0)
        binding.results.statusSpinner.setSelection(viewModel.statusSpinnerPosition.value)
        binding.results.date.text = result.doneDate
        binding.results.newDate.text = result.doneDate
        binding.results.newMeters1.setText(result.zone1)
        binding.results.newMeters2.setText(result.zone2)
        binding.results.newMeters3.setText(result.zone3)
        binding.results.note.setText(result.note)
        binding.results.phone.setText(result.phoneNumber)
        binding.results.checkBox.isChecked = result.is_main == 1

        if (!result.photo.isNullOrEmpty()) {
            imageAdapter.addSavedPhoto(Uri.parse(result.photo))
        }
        val type = if (viewModel.statusSpinnerPosition.value == 0) {
            "2"
        } else {
            "3"
        }

        if (!result.dataSource.isNullOrEmpty()) {
            val spinnerPosition: Int =
                sourceAdapter.getPosition(viewModel.getSourceName(result.dataSource!!, type))
            binding.results.sourceSpinner.setSelection(spinnerPosition)
        } else {
            binding.results.sourceSpinner.setSelection(0)
        }
        isResultSaved = true
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt("index", viewModel.customerIndex.value)
        //declare values before saving the state
        super.onSaveInstanceState(savedInstanceState)
    }

    private fun getBasicInfo(basicInfo: BasicInfo) {
         binding.personalAccount?.text = basicInfo.personalAccount
         binding.address?.text = basicInfo.address
         binding.name?.text = basicInfo.name
         binding.counter?.text = basicInfo.counter
         binding.otherInfo?.text = basicInfo.other
         isFirstLoad = false
    }

    private fun updateView(tdHash: Map<String,String>) {
        binding.infoTable.removeAllViews()

        tdHash.forEach { (key, value) ->
            if (key.isNotEmpty()) {
                createRow(key, value)
            }
        }
    }

    private fun createRow(name: String, data: String) {
        val inflater = LayoutInflater.from(activity)
        val row: TableRow = inflater.inflate(R.layout.user_info_row, null) as TableRow
        val nameText: TextView = row.findViewById(R.id.name)
        nameText.text = name

        when (name) {
            "Телефон" -> {
                val text: TextView = row.findViewById(R.id.data)
                text.text = data
                Linkify.addLinks(text, Linkify.PHONE_NUMBERS)
                text.linksClickable = true
            }
            else -> {
                val text: TextView = row.findViewById(R.id.data)
                text.text = data
            }
        }
        binding.infoTable.addView(row)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        binding.results.date.text = sdformat.format(calendar.time)
        binding.results.newDate.text = sdformat.format(calendar.time)
    }

    private fun saveResult() {
        if (binding.results.newMeters1.text.isNotEmpty() || (viewModel.statusSpinnerPosition.value == 1 && viewModel.sourceSpinnerPosition.value != 0)) {
            val date1: String = binding.results.newDate.text.toString()
            //возможно убрать все отсюда
            val zone1 = binding.results.newMeters1.text.toString()
            val zone2 = binding.results.newMeters2.text.toString()
            val zone3 = binding.results.newMeters3.text.toString()

            val note = binding.results.note.text.toString()
            val phoneNumber = binding.results.phone.text.toString()
            val lat = binding.lat.text.toString()
            val lng = binding.lng.text.toString()
            val isMainPhone = if (binding.results.checkBox.isChecked) {
                1
            } else {
                0
            }
            val photo = if (imageUri.toString().length > 4) {
                imageUri.toString()
            } else {
                null
            }
            val currentDateAndTime =
                SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            viewModel.saveEditTiming(firstEditDate, currentDateAndTime)

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.saveResults(
                    date1,
                    source,
                    source2.joinToString(),
                    zone1,
                    zone2,
                    zone3,
                    note,
                    phoneNumber,
                    isMainPhone,
                    lat,
                    lng,
                    photo
                )
            }
            isResultSaved = true
            Toast.makeText(requireContext(), "Результати збережені", Toast.LENGTH_SHORT).show()
        } else if (binding.results.statusSpinner.selectedItemPosition == 1 && binding.results.sourceSpinner.selectedItemPosition == 0) {
            Toast.makeText(requireContext(), "Ви забули вказати джерело", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Ви не ввели нові показники", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun showIconsDialog(iconsText: String) {
        val currentIcons = iconsText.substringAfter(" ")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Повідомлення")
        var i = 0
        var message = ""

        do {
            val icon = getEmojiByUnicode(icons[i].emoji!!)
            if (currentIcons.contains(icon)) {
                message += "$icon  ${icons[i].hint}\n"
            }
            i++
        } while (i < icons.size)

        builder.setMessage(message)

        builder.setPositiveButton("Ок") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}


}