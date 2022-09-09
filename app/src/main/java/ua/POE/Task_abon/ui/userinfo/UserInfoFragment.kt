package ua.POE.Task_abon.ui.userinfo

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
import com.androidbuts.multispinnerfilter.KeyPairBoolData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.text.StringEscapeUtils
import ua.POE.Task_abon.R
import ua.POE.Task_abon.data.entities.Catalog
import ua.POE.Task_abon.databinding.FragmentUserInfoBinding
import ua.POE.Task_abon.ui.MainActivity
import ua.POE.Task_abon.utils.*
import java.io.File
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class UserInfoFragment : Fragment(), AdapterView.OnItemSelectedListener, View.OnClickListener,
    DatePickerDialog.OnDateSetListener {

    private var binding: FragmentUserInfoBinding by autoCleared()
    private val viewModel: UserInfoViewModel by viewModels()
    private var taskId: String? = "no"
    private var filial: String? = "no"
    private var num: String? = "num"
    private var index: Int? = 1
    private var count: Int? = 1
    private var positionOf: Int = 0
    private val fieldsArray: ArrayList<String> = ArrayList()
    private val basicFieldsTxt = ArrayList<String>()
    private val calendar = Calendar.getInstance(TimeZone.getDefault())
    private val myFormat = "dd.MM.yyyy"
    private val sdformat = SimpleDateFormat(myFormat, Locale.US)
    private var statusSpinnerPosition: Int? = null
    private var numbpers = ""
    var family = ""
    var adress = ""
    var numbersField = ""
    var iconsLs = ""
    private var isFirstLoad = false
    private var type = ""
    var counter = ""
    var zoneCount = ""
    var capacity = ""
    var avgUsage = ""
    var source = ""
    private var source2 = ArrayList<String>()
    private var isResultSaved = false
    private var time = 0
    var massiv = ArrayList<String>()
    var massiv2 = ArrayList<KeyPairBoolData>()
    private val operators by lazy { viewModel.getOperatorsList() }
    lateinit var sourceAdapter: ArrayAdapter<String>

    //  lateinit var sourceAdapter2 : ArrayAdapter<String>
    lateinit var catalog: List<Catalog>
    lateinit var catalog2: List<Catalog>
    lateinit var locationManager: LocationManager
    private val imageAdapter by lazy {
        ImageAdapter(requireContext(), items, uri)
    }
    private val items = ArrayList<Image>()
    private val uri = ArrayList<String>()
    lateinit var tempImage: File
    private var imageUri: Uri? = null
    private var icons = ArrayList<Icons>()
    private var isEdit: Boolean = false
    private var firstEditDate: String = ""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.title = "Інформація"

        if (arguments != null) {
            taskId = requireArguments().getString("taskId")
            filial = requireArguments().getString("filial")
            num = requireArguments().getString("num")
            index = requireArguments().getInt("id")
            count = requireArguments().getInt("count")
            isFirstLoad = requireArguments().getBoolean("isFirstLoad")
        }

        if (savedInstanceState != null) {
            index = savedInstanceState.getInt("index")
        }
        firstEditDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        //читаем иконки
        icons = resources.getRawTextFile(R.raw.icons)

        try {
            binding.results.newMeters1.doAfterTextChanged {
                if (!binding.results.newMeters1.text.isNullOrEmpty())
                    binding.results.difference1.text =
                        (it.toString().toInt() - binding.results.previousMeters1.text.toString()
                            .toInt()).toString()
            }
            binding.results.newMeters2.doAfterTextChanged {
                if (!binding.results.newMeters2.text.isNullOrEmpty())
                    binding.results.difference2.text =
                        (it.toString().toInt() - binding.results.previousMeters2.text.toString()
                            .toInt()).toString()
            }
            binding.results.newMeters3.doAfterTextChanged {
                if (!binding.results.newMeters3.text.isNullOrEmpty())
                    binding.results.difference3.text =
                        (it.toString().toInt() - binding.results.previousMeters3.text.toString()
                            .toInt()).toString()
            }
        } catch (e: NumberFormatException) {
        }

        viewModel.isTrueEdit.observe(viewLifecycleOwner) {
            //Log.d("testim", it.toString())
            isEdit = it
        }

        taskId?.let { getBasicInfo(it) }
        isFirstLoad = false

        checkPermissions()

        val spinner = binding.blockName

        val blockName: MutableList<String> = viewModel.getBlockNames()
        blockName.add(0, "Результати")

        val adapter = context?.let {
            ArrayAdapter(
                it,
                android.R.layout.simple_spinner_dropdown_item,
                blockName
            )
        }
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        binding.previous.setOnClickListener(this)
        binding.next.setOnClickListener(this)

        binding.results.statusSpinner.onItemSelectedListener = this

        //imageAdapter = ImageAdapter(requireContext(), items, uri)
        binding.results.photoLayout.adapter = imageAdapter
        addAddButton()

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

        //requireActivity().startService(TimerService.getIntent(requireActivity()))

    }

    override fun onStart() {
        super.onStart()
        requireActivity().startService(TimerService.getIntent(requireActivity(), time))
        requireActivity().registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getIntExtra(TimerService.TIME_EXTRA, 0)
                //Log.d("testim", time.toString())
        }

    }

    override fun onStop() {
        super.onStop()
       /* if (isEdit) {
            //viewModel.saveEditTime(taskId!!,index.toString(), time)
            val currentDateAndTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            viewModel.saveEndEditDate(taskId!!, index.toString(), currentDateAndTime)
        } else {
            time = 0
        }*/
        requireActivity().unregisterReceiver(updateTime)
        requireActivity().stopService(TimerService.getIntent(requireActivity(), time))
    }

    private fun addAddButton() {
        val selectedImage =
            Uri.parse("android.resource://" + requireActivity().packageName + "/" + R.drawable.ic_add_photo)
        val i = Image()
        i.setURI(selectedImage)
        items.add(i)
        imageAdapter.notifyDataSetChanged()
    }

    private fun addAddButton(uri: Uri) {
        val i = Image()
        i.setURI(uri)
        items.add(i)
        imageAdapter.notifyDataSetChanged()
    }

    private fun deletePhoto() {
        items.clear()
        addAddButton()
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
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
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


            //val imageUri = FileProvider.getUriForFile(requireContext(), "ua.POE.Task_abon.fileprovider", tempImage)
            val i = Image()
            i.setURI(imageUri)
            items.add(i)
            uri.add(imageUri.toString())
            imageAdapter.notifyDataSetChanged()

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
        massiv.clear()
        massiv2.clear()

        //positionOf всегда 0?
        catalog = if (positionOf == 0) {
            viewModel.getSourceList("2")
        } else {
            viewModel.getSourceList("3")
        }

        massiv.add("-Не вибрано-")
        for (i in catalog) {
            massiv.add(i.text!!)
        }

        sourceAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            massiv
        )

        binding.results.sourceSpinner.adapter = sourceAdapter
        binding.results.sourceSpinner.onItemSelectedListener = this

        catalog2 = viewModel.getSourceList("4")

        // massiv2.add("-Не выбрано-")
        val result = if (!savedConditions.isNullOrEmpty()) {
            savedConditions.split(",").map { it.trim() }
        } else {
            val array = viewModel.getCheckedConditions(taskId!!, index!!)
            array.split(",").map { it.trim() }
        }

        for (i in catalog2) {
            if (i.code.toString() in result) {
                massiv2.add(KeyPairBoolData(i.text!!, true))
            } else {
                massiv2.add(KeyPairBoolData(i.text!!, false))
            }
        }

        val multipleSpinner = binding.results.sourceSpinner2
        multipleSpinner.isSearchEnabled = false
        multipleSpinner.isShowSelectAllButton = true
        multipleSpinner.isColorSeparation = false
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES ->
                multipleSpinner.setBackgroundColor(Color.GRAY)
            Configuration.UI_MODE_NIGHT_NO ->
                multipleSpinner.setBackgroundColor(Color.WHITE)
        }

        multipleSpinner.setClearText("Очистити все")
        multipleSpinner.setItems(
            massiv2
        ) { items ->
            source2.clear()
            for (i in items.indices) {
                for (catalog in catalog2) {
                    if (items[i].name == catalog.text) {
                        source2.add(catalog.code!!)
                    }
                }
            }
        }
        multipleSpinner.hintText = "Можливий вибір декількох пунктів:"

        /*sourceAdapter2 = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            massiv2
        )*/
        // binding.results.sourceSpinner2.adapter = sourceAdapter2
        //binding.results.sourceSpinner2.onItemSelectedListener = this
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
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
            R.id.mybutton -> {
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
                    askAboutCoords()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun askAboutCoords() {
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    if (binding.results.phone.text.isNotEmpty() && (binding.results.phone.text.take(
                            3
                        ).toString() !in operators || binding.results.phone.text.length < 10)
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
                    if (binding.results.phone.text.isNotEmpty() && (binding.results.phone.text.take(
                            3
                        ).toString() !in operators || binding.results.phone.text.length < 10)
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

    private fun registerReceivers() {
        requireActivity().startService(TimerService.getIntent(requireActivity(), time))
        requireActivity().registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
    }

    private fun unRegisterReceivers() {
        requireActivity().unregisterReceiver(updateTime)
        requireActivity().stopService(TimerService.getIntent(requireActivity(), time))
    }

    private fun goPrevious() {
        time = 0
        unRegisterReceivers()
        registerReceivers()
        index = if (index != 1) {
            index!!.minus(1)
        } else {
            count
        }
        if (fieldsArray.isNotEmpty())
            updateView(fieldsArray)
        taskId?.let { getBasicInfo(it) }
    }

    private fun goNext() {
        time = 0
        unRegisterReceivers()
        registerReceivers()
        index = if (index != count) {
            index!!.plus(1)
        } else {
            1
        }
        if (fieldsArray.isNotEmpty())
            updateView(fieldsArray)
        taskId?.let { getBasicInfo(it) }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val selectedItem = parent.getItemAtPosition(position).toString()
        when (parent.id) {
            R.id.block_name -> {
                if (selectedItem != "Результати") {
                    fieldsArray.clear()
                    val fields = taskId?.let {
                        viewModel.getFieldsByBlockName(selectedItem, it)
                    }
                    for (element in fields!!) {
                        element.fieldName?.let { fieldsArray.add(it) }
                    }
                    updateView(fieldsArray)
                    binding.infoTables.visibility = VISIBLE
                    binding.results.root.visibility = GONE
                } else {
                    binding.infoTables.visibility = GONE
                    binding.results.root.visibility = VISIBLE
                    loadResultTab()
                }
            }
            R.id.source_spinner -> {
                try {
                    source = if (position != 0) {
                        // val position = position.plus(1)
                        catalog[position - 1].code!!
                    } else ""
                } catch (e: Exception) {
                }

            }
            /*R.id.source_spinner2 -> {
                try {
                    source2 = if (position != 0) {
                        // val position = position.plus(1)
                        catalog2[position].code!!
                    } else ""
                } catch (e: Exception) {

                }

            }*/
            R.id.status_spinner -> {
                statusSpinnerPosition = position
                updateSourceSpinner(position)
            }
        }
    }

    private fun updateSourceSpinner(position: Int) {
        massiv.clear()
        catalog = if (position == 0) {
            viewModel.getSourceList("2")
        } else {
            viewModel.getSourceList("3")
        }
        massiv.add("-Не вибрано-")
        for (i in catalog) {
            massiv.add(i.text!!)
        }
        sourceAdapter.notifyDataSetChanged()

    }

    private fun loadResultTab() {

        binding.results.date.setOnClickListener(this)
        binding.results.newDate.setOnClickListener(this)

        val techHash = viewModel.getTechInfoTextByFields("$taskId", index!!)

        val contr = StringBuilder()

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
                    if (!iconsLs.isNullOrEmpty()) {
                        val text = getNeededEmojis(icons, iconsLs)
                        createRow("Лічильник", "$counter $text", true)
                        iconsLs = ""
                    } else {
                        createRow("Лічильник", counter, true)
                    }
                }
                "Rozr" -> {
                    capacity = value
                }
                "contr_date" -> {
                    binding.results.contrDate.text = value
                    contr.append(" $value")
                }
                "contr_pok" -> {
                    contr.append(" $value")
                }
                "contr_name" -> {
                    contr.append(" $value")
                }
            }
        }



        binding.results.contrText.text =
            resources.getString(R.string.contr_pokaz) + contr.toString()
        binding.results.contrText.setTextColor(Color.YELLOW)
        binding.results.contrText.setTypeface(binding.results.contrText.typeface, Typeface.BOLD)

        try {
            val result = viewModel.getResult(taskId!!, index!!)
            positionOf = result.notDone!!.toInt()
            binding.results.statusSpinner.setSelection(positionOf)
            loadSpinners(result.point_condition)
            binding.results.date.text = result.doneDate
            binding.results.newDate.text = result.doneDate
            binding.results.newMeters1.setText(result.zone1)
            binding.results.newMeters2.setText(result.zone2)
            binding.results.newMeters3.setText(result.zone3)
            binding.results.note.setText(result.note)
            binding.results.phone.setText(result.phoneNumber)
            binding.results.checkBox.isChecked = result.is_main == 1
            deletePhoto()
            if (!result.photo.isNullOrEmpty()) {
                addAddButton(Uri.parse(result.photo))
            }
            val type = if (positionOf == 0) {
                "2"
            } else {
                "3"
            }

            if (!result.dataSource.isNullOrEmpty()) {
                val spinnerPosition: Int =
                    sourceAdapter.getPosition(viewModel.getSourceName(result.dataSource!!, type))
                //Log.d("errortestim", viewModel.getSourceName(result.dataSource!!, type))
                //val spinnerPosition2 : Int = sourceAdapter2.getPosition(viewModel.getNoteName(result.point_condition!!))
                binding.results.sourceSpinner.setSelection(spinnerPosition)
                //binding.results.sourceSpinner2.setSelection(spinnerPosition2 - 1)
            } else {
                binding.results.sourceSpinner.setSelection(0)
            }
            isResultSaved = true

        } catch (e: Exception) {
            //Log.d("errortestim", e.message.toString())
            try {
                positionOf = 0
                loadSpinners("")
                binding.results.date.text = sdformat.format(calendar.time)
                binding.results.newDate.text = sdformat.format(calendar.time)
                binding.results.statusSpinner.setSelection(0)
                binding.results.sourceSpinner.setSelection(0)
                // binding.results.sourceSpinner2.setSelection(0)
                binding.results.newMeters1.setText("", TextView.BufferType.EDITABLE)
                binding.results.newMeters2.setText("", TextView.BufferType.EDITABLE)
                binding.results.newMeters3.setText("", TextView.BufferType.EDITABLE)
                binding.results.checkBox.isChecked = false
                binding.results.difference1.text = ""
                binding.results.note.setText("")
                binding.results.phone.setText("")
                binding.results.checkBox.isChecked = false
                isResultSaved = false
                deletePhoto()
                imageAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                //Log.d("errortestim", e.message.toString())
            }
        }

    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        index?.let { savedInstanceState.putInt("index", it) }
        //declare values before saving the state
        super.onSaveInstanceState(savedInstanceState)
    }

    private fun getBasicInfo(taskId: String) {
        binding.basicTable.removeAllViews()
        val fields = viewModel.getFieldsByBlockName("", taskId)
        for (element in fields) {
            element.fieldName?.let { basicFieldsTxt.add(it) }
        }
        val tdHash = viewModel.getTextFieldsByBlockName(basicFieldsTxt, "TD$taskId", index!!)

        var fieldCounter = 1
        val stringBuilder = StringBuilder()
        var opora = ""

        tdHash.forEach { (key, value) ->
            if (key.isNotEmpty()) {
                when (key) {
                    "О/р" -> {
                        //createRow(key, value, true)
                        numbersField = key
                        numbpers = value;
                    }
                    "Значки о/р" -> {
                        if (value.isNotEmpty()) {
                            var text = getNeededEmojis(icons, value)
                            createRow(numbersField, "$numbpers $text", true)
                        } else {
                            createRow(numbersField, numbpers, true)
                        }
                    }
                    "Адреса" -> {
                        createRow(key, value, true)
                        adress = value
                    }
                    "ПІБ" -> {
                        createRow(key, value, true)
                        family = value
                    }
                    "Опора" -> {
                        opora = "Оп.$value"
                    }
                    "Значки лічильника" -> {
                        if (value.isNotEmpty()) {
                            iconsLs = value
                        }
                    }
                    else -> {
                        if (!key.contains("Значки".toLowerCase()))
                            stringBuilder.append("$value ")
                    }
                }
                /*if (fieldCounter>3) {
                    stringBuilder.append("$value ")
                } else {
                    createRow(key, value, true)
                }*/
                fieldCounter++
            }
        }
        createRow("Інше/Фiдер", stringBuilder.append(opora).toString(), true)
        if (!isFirstLoad) {
            loadResultTab()
        }

        basicFieldsTxt.clear()
        tdHash.clear()
    }

    private fun updateView(fieldsArray: ArrayList<String>) {
        val tdHash = viewModel.getTextFieldsByBlockName(fieldsArray, "TD$taskId", index!!)
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
                        showIconsDialog(data.split(" ")[1])
                    } catch (e: IndexOutOfBoundsException) {
                    }
                }
            }
            name == "О/р" -> {
                val text: TextView = row.findViewById(R.id.data)
                text.text = data
                text.setOnClickListener {
                    try {
                        showIconsDialog(data.split(" ")[1])
                    } catch (e: IndexOutOfBoundsException) {
                    }
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


        if (binding.results.newMeters1.text.isNotEmpty() || (binding.results.statusSpinner.selectedItemPosition == 1 && binding.results.sourceSpinner.selectedItemPosition != 0)) {
            val date1: String = binding.results.newDate.text.toString()
            val isDone: String = statusSpinnerPosition.toString()
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
            val currentDateAndTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            viewModel.saveEditTiming(taskId!!, index.toString(), time, firstEditDate, currentDateAndTime)

            CoroutineScope(Dispatchers.IO).launch {
                viewModel.saveResults(
                    taskId!!,
                    index!!,
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