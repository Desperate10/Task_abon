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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.POE.Task_abon.R
import ua.POE.Task_abon.databinding.FragmentUserInfoBinding
import ua.POE.Task_abon.domain.model.BasicInfo
import ua.POE.Task_abon.domain.model.Catalog
import ua.POE.Task_abon.domain.model.Icons
import ua.POE.Task_abon.domain.model.Image
import ua.POE.Task_abon.presentation.MainActivity
import ua.POE.Task_abon.presentation.adapters.ImageAdapter
import ua.POE.Task_abon.utils.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class UserInfoFragment : Fragment(), AdapterView.OnItemSelectedListener, View.OnClickListener,
    DatePickerDialog.OnDateSetListener {

    private var binding: FragmentUserInfoBinding by autoCleaned()
    private val viewModel: UserInfoViewModel by viewModels()
    private var taskId: Int = 0
    private var filial: String? = null
    private var num: String? = null
    private var index = 1
    private var count = 1
    private var fieldsArray = listOf<String>()
    private val basicFieldsTxt = ArrayList<String>()
    private val calendar = Calendar.getInstance(TimeZone.getDefault())
    private val myFormat = "dd.MM.yyyy"
    private val sdformat = SimpleDateFormat(myFormat, Locale.getDefault())

    // private var statusSpinnerPosition: Int = 0
    private var numbpers = ""
    private var isFirstLoad = false
    var family = ""
    var adress = ""
    var numbersField = ""
    var iconsLs = ""
    private var type = ""
    var counter = ""
    var zoneCount = ""
    var capacity = ""
    var avgUsage = ""
    var source = ""
    private var source2 = ArrayList<String>()
    private var isResultSaved = false
    var massiv = ArrayList<String>()
    var massiv2 = ArrayList<KeyPairBoolData>()
    private val operators by lazy { viewModel.getOperatorsList() }
    lateinit var sourceAdapter: ArrayAdapter<String>

    lateinit var catalog: List<Catalog>
    lateinit var featureList: List<Catalog>
    lateinit var locationManager: LocationManager
    private val imageAdapter: ImageAdapter by autoCleaned {
        ImageAdapter(requireContext(), items, uri)
    }
    private val items = ArrayList<Image>()
    private val uri = ArrayList<String>()
    private lateinit var tempImage: File
    private var imageUri: Uri? = null
    private var icons = ArrayList<Icons>()
    private var firstEditDate: String = ""
    private var zone1watcher: TextWatcher? = null
    private var zone2watcher: TextWatcher? = null
    private var zone3watcher: TextWatcher? = null

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.statusSpinnerPosition
                    .flatMapLatest { viewModel.getSourceList() }
                    .flowOn(Dispatchers.IO)
                    .collect {
                        catalog = it
                        updateSourceSpinner(it)
                    }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sourceSpinnerPosition.collect {
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
                        //добавить запрос во viewModel для получения названия полей
                        //и попробовать так
                        viewModel.getCustomerBasicInfo(taskId, index, icons)
                    }.collectLatest {
                        getBasicInfo(it)
                    }
            }
        }
        /*viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.customerIndex.flatMapMerge { index ->
                    viewModel.getFieldsByBlockName("", taskId).asFlow()
                }.flatMapMerge {
                    viewModel.getTextFieldsByBlockName(it, "TD$taskId", index)
                }
            }
        }*/
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.title = "Інформація"

        if (savedInstanceState != null) {
            index = savedInstanceState.getInt("index")
        }

        arguments?.let {
            taskId = arguments?.getInt("taskId") ?: 0
            filial = arguments?.getString("filial")
            num = arguments?.getString("num")
            index = arguments?.getInt("id") ?: throw NullPointerException("index is null")
            count = arguments?.getInt("count") ?: throw NullPointerException("count is null")
            isFirstLoad = requireArguments().getBoolean("isFirstLoad")
        }
        viewModel.setSelectedCustomer(index)

        firstEditDate = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())

        //читаем иконки
        icons = resources.getRawTextFile(R.raw.icons)

        //getBasicInfo(taskId)


        checkPermissions()
        setupSecondarySpinners()
        registerClickListeners()
        setupImageAdapter()
        observeViewModel()
    }

    private fun registerClickListeners() {
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
        binding.results.statusSpinner.onItemSelectedListener = this
        //sourcespinner
        sourceAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            massiv
        )
        binding.results.sourceSpinner.adapter = sourceAdapter
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
        binding.results.newMeters1.removeTextChangedListener(zone1watcher)
        binding.results.newMeters2.removeTextChangedListener(zone2watcher)
        binding.results.newMeters3.removeTextChangedListener(zone3watcher)
    }

    private fun takePhoto() {
        val filename = filial + "_" + numbpers + "_"
        val storageDirectory: File? =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        try {
            tempImage = File.createTempFile(filename, ".jpg", storageDirectory)
            imageUri = FileProvider.getUriForFile(
                requireContext(),
                "ua.POE.Task_abon.fileprovider",
                tempImage
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN -> {
                    val clip = ClipData.newUri(
                        requireActivity().contentResolver,
                        "A photo",
                        imageUri
                    )
                    intent.clipData = clip
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                else -> {
                    val resInfoList: List<ResolveInfo> = requireActivity().packageManager
                        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                    for (resolveInfo in resInfoList) {
                        val packageName = resolveInfo.activityInfo.packageName
                        requireActivity().grantUriPermission(
                            packageName, imageUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    }
                }
            }
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
                /* locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1000,
                    1f, locationListener
                )*/

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

    private fun loadSpinners(savedConditions: String?) {

        //updateSourceSpinner(catalog)
        /*binding.results.sourceSpinner.adapter = sourceAdapter
        binding.results.sourceSpinner.onItemSelectedListener = this*/


        // massiv2.add("-Не выбрано-")
        val result = if (!savedConditions.isNullOrEmpty()) {
            savedConditions
        } else {
            viewModel.getCheckedConditions(taskId, index)
        }.split(",").map { it.trim() }
        massiv2.clear()
        for (feature in featureList) {
            if (feature.code.toString() in result) {
                massiv2.add(KeyPairBoolData(feature.text!!, true))
            } else {
                massiv2.add(KeyPairBoolData(feature.text!!, false))
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
            setItems(
                massiv2
            ) { items ->
                source2.clear()
                for (i in items.indices) {
                    for (feature in featureList) {
                        if (items[i].name == feature.text) {
                            feature.code?.let { source2.add(it) }
                        }
                    }
                }
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
        if (v.id == R.id.previous) {
            if (!isResultSaved && (binding.results.newMeters1.text.isNotEmpty() || binding.results.sourceSpinner.selectedItemPosition == 1)) {
                showSaveOrNotDialog(false)
            } else {
                goPrevious()
            }
        } else if (v.id == R.id.next) {
            if (!isResultSaved && (binding.results.newMeters1.text.isNotEmpty() || binding.results.sourceSpinner.selectedItemPosition == 1)) {
                showSaveOrNotDialog(true)
            } else {
                goNext()
            }
        } else if (v.id == R.id.date || v.id == R.id.new_date) {
            val dialog = DatePickerDialog(
                requireContext(), this,
                calendar[Calendar.YEAR], calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            )
            dialog.show()
        }
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

    //сбрасываем таймер при переключении юзера
    private fun resetTimer() {
        viewModel.time = 0
    }

    private fun goPrevious() {
        viewModel.selectPreviousCustomer()
        selectCustomer(R.id.previous)
    }

    private fun goNext() {
        viewModel.selectNextCustomer()
        selectCustomer(R.id.next)
    }

    private fun selectCustomer(viewId: Int) {
        resetTimer()
        firstEditDate = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        index = if (viewId == R.id.next) {
            if (index != count) {
                index.plus(1)
            } else {
                1
            }
        } else {
            if (index != 1) {
                index.minus(1)
            } else {
                count
            }
        }
        viewModel.setSelectedCustomer(index)

        if (fieldsArray.isNotEmpty())
            updateView(fieldsArray)
        //getBasicInfo(taskId)
    }

    /**
     * @param
     *
     */
    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        when (parent.id) {
            R.id.block_name -> {
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem != "Результати") {
                    val fields = viewModel.getFieldsByBlockName(selectedItem, taskId)
                    fieldsArray = fields.map { it.fieldName.toString() }
                    updateView(fieldsArray)
                    binding.infoTables.visibility = VISIBLE
                    binding.results.root.visibility = GONE
                } else {
                    loadResultTab()
                    binding.infoTables.visibility = GONE
                    binding.results.root.visibility = VISIBLE
                }
            }
            R.id.status_spinner -> {
                viewModel.setStatusSpinnerPosition(position)
            }
            R.id.source_spinner -> {
                viewModel.setSourceSpinnerPosition(position)
            }
        }
    }

    private fun updateSourceSpinner(catalogList: List<Catalog>) {
        massiv.clear()
        massiv.add("-Не вибрано-")
        for (i in catalogList) {
            massiv.add(i.text!!)
        }
        sourceAdapter.notifyDataSetChanged()
    }

    private fun loadResultTab() {

        val techHash = viewModel.getTechInfoTextByFields(taskId, index)
        val controlInfo = StringBuilder()

        techHash.forEach { (key, value) ->
            when (key) {
                "TimeZonalId" -> {
                    zoneCount = value
                    when (value) {
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
                }
                "Lastdate" -> {
                    binding.results.lastDate.text = value
                }
                "Lastlcount" -> {
                    val pokaz: List<String> = value.split("/")
                    binding.results.previousMeters1.text = pokaz[0]
                    if (pokaz.size == 2) {
                        binding.results.previousMeters2.text = pokaz[1]
                    } else if (pokaz.size == 3) {
                        binding.results.previousMeters2.text = pokaz[1]
                        binding.results.previousMeters3.text = pokaz[2]
                    }
                }
                "srnach" -> {
                    avgUsage = value
                    binding.results.differenceText.text = "Расход\nСредн $value"

                }
                "type" -> {
                    type = value
                }
                "Counter_numb" -> {
                    counter = value
                    /*if (iconsLs.isNotEmpty()) {
                        val text = getNeededEmojis(icons, iconsLs)
                        createRow("Лічильник", "$counter $text", true)
                        iconsLs = ""
                    } else {
                        createRow("Лічильник", counter, true)
                    }*/
                }
                "Rozr" -> {
                    capacity = value
                }
                "contr_date" -> {
                    binding.results.contrDate.text = value
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

        binding.results.contrText.text =
            resources.getString(R.string.contr_pokaz) + controlInfo.toString()
        binding.results.contrText.setTextColor(Color.YELLOW)
        binding.results.contrText.setTypeface(binding.results.contrText.typeface, Typeface.BOLD)
        imageAdapter.deletePhoto(requireContext())
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
        try {
            val result = viewModel.getResult(taskId, index)
            //statusSpinnerPosition = result.notDone?.toInt() ?: 0
            viewModel.setStatusSpinnerPosition(result.notDone?.toInt() ?: 0)
            binding.results.statusSpinner.setSelection(viewModel.statusSpinnerPosition.value)
            loadSpinners(result.point_condition)
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

        } catch (e: Exception) {
            try {
                viewModel.setStatusSpinnerPosition(0)
                loadSpinners("")
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
            } catch (e: Exception) {
                //Log.d("errortestim", e.message.toString())
            }
        }

    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt("index", index)
        //declare values before saving the state
        super.onSaveInstanceState(savedInstanceState)
    }

    private fun getBasicInfo(basicInfo: BasicInfo) {
        //binding.basicTable.removeAllViews()

        /*basicInfo.forEach {
            createRow(it.first, it.second, true)
        }*/
         binding.personalAccount?.text = basicInfo.personalAccount
         binding.address?.text = basicInfo.address
         binding.name?.text = basicInfo.name
         binding.counter?.text = basicInfo.counter
         binding.otherInfo?.text = basicInfo.other


        if (!isFirstLoad) {
            loadResultTab()
        }
        isFirstLoad = false
    }

    private fun updateView(fieldsArray: List<String>) {
        val tdHash = viewModel.getTextFieldsByBlockName(fieldsArray, "TD$taskId", index)
        binding.infoTable.removeAllViews()

        tdHash.forEach { (key, value) ->
            if (key.isNotEmpty()) {
                createRow(key, value, false)
            }
        }

        tdHash.clear()
    }

    private fun createRow(name: String, data: String, isBasic: Boolean) {
        val inflater = LayoutInflater.from(activity)

        val row: TableRow = inflater.inflate(R.layout.user_info_row, null) as TableRow

        val nameText: TextView = row.findViewById(R.id.name)
        nameText.text = name
        when {
            name == "Лічильник" -> {
                val text: TextView = row.findViewById(R.id.data)
                text.text = data
                text.setOnClickListener {
                    try {
                        showIconsDialog(data.substringAfter(" "))

                    } catch (e: IndexOutOfBoundsException) {
                    }
                }
            }
            name == "О/р" -> {
                val text: TextView = row.findViewById(R.id.data)
                text.text = data
                text.setOnClickListener {

                }
            }
            name != "Телефон" -> {
                val text: TextView = row.findViewById(R.id.data)
                text.text = data
            }
            else -> {
                val text: TextView = row.findViewById(R.id.data)
                text.text = data
                Linkify.addLinks(text, Linkify.PHONE_NUMBERS)
                text.linksClickable = true
            }
        }

        if (!isBasic)
            binding.infoTable.addView(row)
        else binding.basicTable.addView(row)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        binding.results.date.text = sdformat.format(calendar.time)
        binding.results.newDate.text = sdformat.format(calendar.time)
    }

    private fun saveResult() {
        //Log.d("testim", "${viewModel.statusSpinnerPosition.value} ${viewModel.sourceSpinnerPosition.value}")

        if (binding.results.newMeters1.text.isNotEmpty() || (viewModel.statusSpinnerPosition.value == 1 && viewModel.sourceSpinnerPosition.value != 0)) {
            val date1: String = binding.results.newDate.text.toString()
            //val isDone: String = statusSpinnerPosition.toString()
            //возможно убрать все отсюда
            val isDone = viewModel.statusSpinnerPosition.value.toString()
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
            viewModel.saveEditTiming(taskId, index.toString(), firstEditDate, currentDateAndTime)
            resetTimer()

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.saveResults(
                    taskId,
                    index,
                    date1,
                    isDone,
                    source,
                    source2.joinToString(),
                    zone1,
                    zone2,
                    zone3,
                    note,
                    phoneNumber,
                    isMainPhone,
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
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Повідомлення")
        var i = 0
        var message = ""

        do {
            val icon = getEmojiByUnicode(icons[i].emoji!!)
            if (iconsText.contains(icon)) {
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