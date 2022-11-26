package ua.POE.Task_abon.presentation.ui.userinfo

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextWatcher
import android.text.util.Linkify
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.androidbuts.multispinnerfilter.KeyPairBoolData
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ua.POE.Task_abon.BuildConfig
import ua.POE.Task_abon.R
import ua.POE.Task_abon.databinding.FragmentUserInfoBinding
import ua.POE.Task_abon.domain.model.BasicInfo
import ua.POE.Task_abon.domain.model.SavedData
import ua.POE.Task_abon.domain.model.TechInfo
import ua.POE.Task_abon.presentation.ui.userinfo.dialog.IconsDialogFragment
import ua.POE.Task_abon.presentation.ui.userinfo.dialog.LocationToggleDialogFragment
import ua.POE.Task_abon.presentation.ui.userinfo.dialog.SaveConfirmationDialogFragment
import ua.POE.Task_abon.presentation.ui.userinfo.dialog.SaveCoordinatesDialogFragment
import ua.POE.Task_abon.presentation.ui.userinfo.listener.ItemSelectedListener
import ua.POE.Task_abon.presentation.ui.userinfo.listener.MyLocationListener
import ua.POE.Task_abon.presentation.ui.userinfo.textwatcher.DiffTextWatcher
import ua.POE.Task_abon.utils.autoCleaned
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class UserInfoFragment : Fragment(), View.OnClickListener,
    DatePickerDialog.OnDateSetListener, ItemSelectedListener, MyLocationListener,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private val args by navArgs<UserInfoFragmentArgs>()
    private var binding: FragmentUserInfoBinding by autoCleaned()
    private val viewModel: UserInfoViewModel by viewModels()
    private val calendar = Calendar.getInstance(TimeZone.getDefault())
    private val date = "dd.MM.yyyy"
    private val dateFormat = SimpleDateFormat(date, Locale.getDefault())
    private var zone1 = ""
    private var zone2 = ""
    private var zone3 = ""
    private var sourceAdapter: ArrayAdapter<String>? = null
    private var locationManager: LocationManager? = null

    private var zone1watcher: TextWatcher? = null
    private var zone2watcher: TextWatcher? = null
    private var zone3watcher: TextWatcher? = null

    private var latestTmpUri: Uri? = null
    private val takeImageResult =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                latestTmpUri?.let { uri ->
                    setPic(uri)
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            viewModel.setSelectedCustomer(savedInstanceState.getInt("index"))
            zone1 = savedInstanceState.getString("zone1") ?: ""
            zone2 = savedInstanceState.getString("zone2") ?: ""
            zone3 = savedInstanceState.getString("zone3") ?: ""
        }

        checkPermissions()
        setupSaveConfirmationDialogFragmentListener()
        setupSaveCoordinatesDialogFragmentListener()
        registerItemListeners()
        registerClickListeners()
        observeViewModel()
    }

    private fun observeViewModel() {

        viewModel.getTask()
        viewModel.setStartEditTime()
        viewModel.getOperatorsList()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sources.collectLatest {
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
                viewModel.basicInfo.collectLatest {
                    if (it != null) {
                        getBasicInfo(it)
                    }
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
                    registerWatchers()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.savedData.collectLatest { savedData ->
                    resetFields()
                    savedData.status?.let { getResultIfExist(savedData) }
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
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveAnswer.collectLatest {
                    if (it.isNotEmpty()) {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    }
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
        binding.results.lastDate.gravity = Gravity.CENTER

        val lastCount: List<String> = it.lastCount.split("/")
        binding.results.previousMeters1.text = lastCount[0]
        if (lastCount.size == ZONE_COUNT_2) {
            binding.results.previousMeters2.text = lastCount[1]
        } else if (lastCount.size == ZONE_COUNT_3) {
            binding.results.previousMeters2.text = lastCount[1]
            binding.results.previousMeters3.text = lastCount[2]
        }
        binding.results.differenceText.text =
            String.format(getString(R.string.diff_template), it.averageUsage)
        binding.results.contrDate.text = it.checkDate
        binding.results.contrText.text =
            String.format(getString(R.string.contr_template), it.inspector)
    }

    private fun registerWatchers() {
        zone1watcher = DiffTextWatcher.registerWatcher(
            binding.results.newMeters1,
            binding.results.difference1,
            binding.results.previousMeters1
        )
        zone2watcher = DiffTextWatcher.registerWatcher(
            binding.results.newMeters2,
            binding.results.difference2,
            binding.results.previousMeters2
        )
        zone3watcher = DiffTextWatcher.registerWatcher(
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

    private fun registerClickListeners() {
        binding.personalAccount.setOnClickListener(this)
        binding.counter.setOnClickListener(this)
        binding.previous.setOnClickListener(this)
        binding.next.setOnClickListener(this)
        binding.results.date.setOnClickListener(this)
        binding.results.newDate.setOnClickListener(this)
        binding.results.addPhoto.setOnClickListener(this)
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

    override fun onStop() {
        super.onStop()
        if (locationManager != null) {
            locationManager?.removeUpdates(this)
            locationManager = null
        }
        removeTextWatchers()
    }

    override fun onLocationChanged(location: Location) {
        binding.lat.text = location.latitude.toString()
        binding.lng.text = location.longitude.toString()
    }

    @SuppressLint("MissingPermission")
    private fun checkPermissions() {
        PermissionX.init(requireActivity())
            .permissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    getString(R.string.explain_permission_text),
                    getString(R.string.yes), getString(R.string.cancel)
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    getString(R.string.forward_to_settings_text),
                    getString(R.string.yes), getString(R.string.cancel)
                )
            }
            .request { allGranted, _, deniedList ->
                if (allGranted) {
                    locationManager =
                        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    locationManager?.let {
                        it.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, EVERY_SECOND,
                            EVERY_10M, this
                        )
                        if (!it.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            LocationToggleDialogFragment.show(parentFragmentManager)
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "${getString(R.string.denied_permissions_text)} $deniedList",
                        Toast.LENGTH_LONG
                    ).show()
                }
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
            hintText = getString(R.string.feature_spinner_hint)
            setClearText(getString(R.string.remove_all))
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
                navigateToTaskDetailFragment()
            }
            R.id.save_customer_data -> {
                if (binding.lat.text != "0.0") {
                    saveResult()
                } else {
                    showSaveCoordinatesDialog()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToTaskDetailFragment() {
        findNavController().popBackStack()
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
                saveResult(true, isNext)
            }
        }
    }

    private fun showSaveCoordinatesDialog() {
        SaveCoordinatesDialogFragment.show(parentFragmentManager)
    }

    private fun setupSaveCoordinatesDialogFragmentListener() {
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
                    || binding.results.sourceSpinner.selectedItemPosition == 1)
        ) {
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
            R.id.add_photo -> {
                pickPhoto()
            }
        }
    }

    private fun pickPhoto() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takeImageResult.launch(uri)
            }
        }
    }

    private fun setPic(uri: Uri) {
        // Get the dimensions of the View
            val targetW: Int = binding.results.addPhoto.width
            val targetH: Int = binding.results.addPhoto.height

            val bmOptions = BitmapFactory.Options().apply {
                // Get the dimensions of the bitmap
                inJustDecodeBounds = true

                val photoW: Int = outWidth
                val photoH: Int = outHeight

                // Determine how much to scale down the image
                val scaleFactor: Int =
                    1.coerceAtLeast((photoW / targetW).coerceAtMost(photoH / targetH))

                // Decode the image file into a Bitmap sized to fill the View
                inJustDecodeBounds = false
                inSampleSize = scaleFactor
            }

            BitmapFactory.decodeStream(
                context?.contentResolver?.openInputStream(uri),
                null,
                bmOptions
            )?.also { bitmap ->
                    binding.results.addPhoto.setImageBitmap(bitmap)
                    binding.results.addPhoto.scaleType = ImageView.ScaleType.CENTER_CROP

            }
    }

    private fun getTmpFileUri(): Uri {
        val filename = args.filial + "_" + binding.personalAccount.text.toString().substringBefore(" ")
            .replace("/", "") + "_"
        val storageDirectory: File? =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val tmpFile = File.createTempFile(filename, ".jpg", storageDirectory).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(
            requireActivity(),
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            tmpFile
        )
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
        latestTmpUri = null
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
        binding.results.date.text = dateFormat.format(calendar.time)
        binding.results.newDate.text = dateFormat.format(calendar.time)
        binding.results.sourceSpinner.setSelection(0)
        binding.results.newMeters1.setText(zone1, TextView.BufferType.EDITABLE)
        binding.results.newMeters2.setText(zone2, TextView.BufferType.EDITABLE)
        binding.results.newMeters3.setText(zone3, TextView.BufferType.EDITABLE)
        zone1 = ""
        zone2 = ""
        zone3 = ""
        binding.results.checkBox.isChecked = false
        binding.results.difference1.text = ""
        binding.results.difference2.text = ""
        binding.results.difference3.text = ""
        binding.results.note.setText("")
        binding.results.phone.setText("")
        viewModel.setResultSavedState(false)
        if (latestTmpUri == null) {
            binding.results.addPhoto.setImageDrawable(null)
        }
    }

    private fun getResultIfExist(savedData: SavedData) {
        viewModel.setStatusSpinnerPosition(savedData.status?.toInt() ?: 0)
        binding.results.statusSpinner.setSelection(viewModel.statusSpinnerPosition.value)
        binding.results.date.text = savedData.date
        binding.results.newDate.text = savedData.date
        binding.results.newMeters1.setText(savedData.zone1)
        if (!savedData.zone2.isNullOrEmpty())
            binding.results.newMeters2.setText(savedData.zone2)
        if (!savedData.zone3.isNullOrEmpty())
            binding.results.newMeters3.setText(savedData.zone3)
        binding.results.note.setText(savedData.note)
        binding.results.phone.setText(savedData.phoneNumber)
        binding.results.checkBox.isChecked = savedData.isMainPhone ?: true

        if (!savedData.photo.isNullOrEmpty() && latestTmpUri == null) {
            binding.results.addPhoto.setImageURI(Uri.parse(savedData.photo))
        }

        val spinnerPosition = sourceAdapter?.getPosition(savedData.source)
        spinnerPosition?.let { binding.results.sourceSpinner.setSelection(it) }
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
    }

    private fun updateView(tdHash: Map<String, String>) {
        binding.infoTable.removeAllViews()
        tdHash.forEach { (key, value) ->
            if (key.isNotEmpty()) {
                createRow(key, value)
            }
        }
    }

    @SuppressLint("InflateParams")
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

    private fun saveResult(selectCustomer: Boolean = false, isNext: Boolean = true) {
        viewModel.saveResults(
            date = binding.results.newDate.text.toString(),
            zone1 = binding.results.newMeters1.text.toString(),
            zone2 = binding.results.newMeters2.text.toString(),
            zone3 = binding.results.newMeters3.text.toString(),
            note = binding.results.note.text.toString().replace("[\\t\\n\\r]+", " "),
            phoneNumber = binding.results.phone.text.toString(),
            lat = binding.lat.text.toString(),
            lng = binding.lng.text.toString(),
            isMainPhone = binding.results.checkBox.isChecked,
            photo = latestTmpUri.toString(),
            selectCustomer = selectCustomer,
            isNext = isNext
        )
    }

    companion object {
        const val ZONE_COUNT_1 = 1
        const val ZONE_COUNT_2 = 2
        const val ZONE_COUNT_3 = 3
        const val EVERY_SECOND = 1000L
        const val EVERY_10M = 10f
    }
}