package ua.POE.Task_abon.presentation.userinfo

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.POE.Task_abon.R
import ua.POE.Task_abon.databinding.FragmentUserInfoBinding
import ua.POE.Task_abon.domain.model.*
import ua.POE.Task_abon.presentation.MainActivity
import ua.POE.Task_abon.presentation.adapters.ImageAdapter
import ua.POE.Task_abon.utils.autoCleaned
import ua.POE.Task_abon.utils.getRawTextFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class UserInfoFragment : Fragment(), View.OnClickListener,
    DatePickerDialog.OnDateSetListener, ItemSelectedListener {

    private var binding: FragmentUserInfoBinding by autoCleaned()
    private val viewModel: UserInfoViewModel by viewModels()
    private var filial: String? = null
    private val calendar = Calendar.getInstance(TimeZone.getDefault())
    private val date = "dd.MM.yyyy"
    private val dateFormat = SimpleDateFormat(date, Locale.getDefault())
    private var zone1 = ""
    private var zone2 = ""
    private var zone3 = ""

    private var isFirstLoad = false

    private lateinit var sourceAdapter: ArrayAdapter<String>

    private var featureList = listOf<Catalog>()
    lateinit var locationManager: LocationManager
    private val imageAdapter: ImageAdapter by autoCleaned {
        ImageAdapter(requireContext(), items, uri)
    }
    private val items = ArrayList<Image>(2)
    private val uri = ArrayList<String>()
    private var imageUri: Uri? = null
    private val icons by lazy {
        resources.getRawTextFile(R.raw.icons)
    }
    private var zone1watcher: TextWatcher? = null
    private var zone2watcher: TextWatcher? = null
    private var zone3watcher: TextWatcher? = null

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sources.collect {
                    setupSourceSpinner(it)
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
                viewModel.result.collect { savedData ->
                    resetFields()
                    savedData?.status?.let { getResultIfExist(savedData) }
                    registerWatchers()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.featureList.collectLatest {
                    featureList = it
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.customerFeatures.collect {
                    loadFeatureSpinner(it)
                }
            }
        }
    }

    private fun preloadResultTab(it: TechInfo) {
        when (it.zoneCount) {
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
            zone1 = savedInstanceState.getString("zone1") ?: ""
            zone2 = savedInstanceState.getString("zone2") ?: ""
            zone3 = savedInstanceState.getString("zone3") ?: ""
        }

        arguments?.let {
            filial = arguments?.getString("filial")
            isFirstLoad = requireArguments().getBoolean("isFirstLoad")
        }

        setupSaveConfirmationDialogFragmentListener()
        setupSaveCoordinatesDialog()
        checkPermissions()
        registerItemListeners()
        registerClickListeners()
        setupImageAdapter()
        observeViewModel()
    }

    private fun registerClickListeners() {
        binding.personalAccount.setOnClickListener(this)
        binding.counter.setOnClickListener(this)
        binding.previous.setOnClickListener(this)
        binding.next.setOnClickListener(this)
        binding.results.date.setOnClickListener(this)
        binding.results.newDate.setOnClickListener(this)
    }

    private fun setupMainBlockSpinner(list: List<String>) {
        binding.blockName.adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                list
            )
    }

    private fun setupSourceSpinner(list: List<String>) {
        sourceAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            list
        )
        binding.results.sourceSpinner.adapter = sourceAdapter
    }

    private fun registerItemListeners() {
        binding.blockName.onItemSelectedListener = this
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
                        "Видаліть фото, щоб створити нове",
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

    private fun loadFeatureSpinner(
        customerFeatures: List<KeyPairBoolData>
    ) {
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
            setItems(customerFeatures) { items ->
                viewModel.setItems(items)
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
                if (binding.lat.text != "0.0") {
                    saveResult()
                } else {
                    showSaveCoordinatesDialog()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showConfirmationDialog(isForward: Boolean) {
        SaveConfirmationDialogFragment.show(parentFragmentManager, isForward)
    }

    private fun setupSaveConfirmationDialogFragmentListener() {
        SaveConfirmationDialogFragment.setupListeners(
            parentFragmentManager,
            this
        ) { isNext, buttonPressed ->
            if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
                saveResult()
            }
            selectCustomer(isNext)
        }
    }

    private fun showSaveCoordinatesDialog() {
        SaveCoordinatesDialogFragment.show(parentFragmentManager)
    }

    private fun setupSaveCoordinatesDialog() {
        SaveCoordinatesDialogFragment.setupListeners(parentFragmentManager, this) {
            when (it) {
                DialogInterface.BUTTON_POSITIVE -> saveResult()
            }
        }
    }

    private fun showIconsDialogFragment(icons: String) {
        IconsDialogFragment.show(parentFragmentManager, icons)
    }

    private fun changeCustomer(isNext: Boolean) {
        if (!viewModel.isResultSaved() && (binding.results.newMeters1.text.isNotEmpty()
                    || binding.results.sourceSpinner.selectedItemPosition == 1)) {
            showConfirmationDialog(isNext)
        } else {
            selectCustomer(isNext)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.previous -> {
                changeCustomer(isNext = false)
            }
            R.id.next -> {
                changeCustomer(isNext = true)
            }
            R.id.personal_account, R.id.counter -> {
                showIconsDialogFragment((v as TextView).text.toString())
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

    private fun selectCustomer(isNext: Boolean) {
        viewModel.selectCustomer(isNext)
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

    private fun resetFields() {
        viewModel.setStatusSpinnerPosition(0)
        binding.results.date.text = dateFormat.format(calendar.time)
        binding.results.newDate.text = dateFormat.format(calendar.time)
        binding.results.statusSpinner.setSelection(0)
        binding.results.sourceSpinner.setSelection(0)
        binding.results.newMeters1.setText(zone1, TextView.BufferType.EDITABLE)
        binding.results.newMeters2.setText(zone2, TextView.BufferType.EDITABLE)
        binding.results.newMeters3.setText(zone3, TextView.BufferType.EDITABLE)
        zone1 = ""
        zone2 = ""
        zone3 = ""
        binding.results.checkBox.isChecked = false
        binding.results.difference1.text = ""
        binding.results.note.setText("")
        binding.results.phone.setText("")
        viewModel.setResultSavedState(false)
        imageAdapter.notifyDataSetChanged()
    }

    private fun getResultIfExist(savedData: SavedData) {
        viewModel.setStatusSpinnerPosition(savedData.status?.toInt() ?: 0)
        binding.results.statusSpinner.setSelection(viewModel.statusSpinnerPosition.value)
        binding.results.date.text = savedData.date
        binding.results.newDate.text = savedData.date
        binding.results.newMeters1.setText(savedData.zone1)
        binding.results.newMeters2.setText(savedData.zone2)
        binding.results.newMeters3.setText(savedData.zone3)
        binding.results.note.setText(savedData.note)
        binding.results.phone.setText(savedData.phoneNumber)
        binding.results.checkBox.isChecked = savedData.isMainPhone ?: true

        if (!savedData.photo.isNullOrEmpty()) {
            imageAdapter.addSavedPhoto(Uri.parse(savedData.photo))
        }
        val type = if (viewModel.statusSpinnerPosition.value == 0) {
            "2"
        } else {
            "3"
        }

        if (!savedData.source.isNullOrEmpty()) {
            val spinnerPosition =
                sourceAdapter.getPosition(viewModel.getSourceName(savedData.source, type))

            binding.results.sourceSpinner.setSelection(spinnerPosition)
        } else {
            binding.results.sourceSpinner.setSelection(0)
        }
        viewModel.setResultSavedState(true)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt("index", viewModel.customerIndex.value)
        savedInstanceState.putString("zone1", binding.results.newMeters1.text.toString())
        savedInstanceState.putString("zone2", binding.results.newMeters2.text.toString())
        savedInstanceState.putString("zone3", binding.results.newMeters3.text.toString())
        //declare values before saving the state
        super.onSaveInstanceState(savedInstanceState)
    }

    private fun getBasicInfo(basicInfo: BasicInfo) {
        binding.personalAccount.text = basicInfo.personalAccount
        binding.address.text = basicInfo.address
        binding.name.text = basicInfo.name
        binding.counter.text = basicInfo.counter
        binding.otherInfo.text = basicInfo.other
        isFirstLoad = false
    }

    private fun updateView(tdHash: Map<String, String>) {
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

        binding.results.date.text = dateFormat.format(calendar.time)
        binding.results.newDate.text = dateFormat.format(calendar.time)
    }

    private fun saveResult() {
        viewModel.saveResults(
            date = binding.results.newDate.text.toString(),
            zone1 = binding.results.newMeters1.text.toString(),
            zone2 = binding.results.newMeters2.text.toString(),
            zone3 = binding.results.newMeters3.text.toString(),
            note = binding.results.note.text.toString(),
            phoneNumber = binding.results.phone.text.toString(),
            lat = binding.lat.text.toString(),
            lng = binding.lng.text.toString(),
            isMainPhone = binding.results.checkBox.isChecked,
            photo = imageUri.toString()
        )
    }

}