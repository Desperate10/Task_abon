package ua.POE.Task_abon.ui.tasks

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable.cancel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ua.POE.Task_abon.R
import ua.POE.Task_abon.data.entities.Task
import ua.POE.Task_abon.databinding.FragmentTasksBinding
import ua.POE.Task_abon.network.MyApi
import ua.POE.Task_abon.network.UploadRequestBody
import ua.POE.Task_abon.network.UploadResponse
import ua.POE.Task_abon.ui.MainActivity
import ua.POE.Task_abon.utils.*
import java.io.*


@AndroidEntryPoint
class TasksFragment : Fragment(), TaskListAdapter.ItemCLickListener, UploadRequestBody.UploadCallback{

     private var binding : FragmentTasksBinding by autoCleared()
     private val viewModel : TaskViewModel by viewModels()
     private lateinit var taskId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        requestPermission()

        // This callback will only be called when MyFragment is at least Started.
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                requireActivity().finishAffinity()
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = "Список завдань"
        //hideKeyboard()

        val linearLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        binding.recyclerview.layoutManager = linearLayoutManager
        viewModel.tasks.observe(viewLifecycleOwner, { tasks ->
            //Log.d("taska", tasks.toString())
            if (tasks.isNotEmpty()) {
                binding.recyclerview.adapter = TaskListAdapter(tasks, this)
                binding.noTasks.visibility = View.GONE
                binding.recyclerview.visibility = View.VISIBLE
            } else {
                binding.noTasks.visibility = View.VISIBLE
                binding.recyclerview.visibility = View.GONE
            }
        })

        binding.choose.setOnClickListener {
            chooseFile()
        }

        viewModel.taskLoadingStatus.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    Toast.makeText(activity, "Завдання успішно вигружено", Toast.LENGTH_SHORT).show()
                }
                Resource.Status.ERROR -> {
                    Toast.makeText(activity, "Помилка вигрузки завдання", Toast.LENGTH_SHORT).show()
                }
                Resource.Status.LOADING -> {
                    Toast.makeText(requireActivity(), "Вигрузка...", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tasksmenu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mybutton -> {
                chooseFile()
                true
            }
            R.id.info -> {
                showInfo()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun requestPermission() {

        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        this.requestPermissions(permissions, 5)
    }


    private fun chooseFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/xml"
        }
        startActivityForResult(intent, PICK_XML_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_XML_FILE && resultCode == Activity.RESULT_OK) {
            CoroutineScope(Dispatchers.IO).launch {
                data?.data?.also { uri ->  viewModel.insert(uri)}
            }
        } else if(requestCode ==2) {
            CoroutineScope(Dispatchers.IO).launch {
                val uri1 = data?.data
                try {
                    val os = requireActivity().contentResolver.openOutputStream(uri1!!)
                    val w: Writer = BufferedWriter(OutputStreamWriter(os, "windows-1251"))
                    val sb = viewModel.createXml(taskId)
                    w.write(sb)
                    w.flush()
                    w.close()
                    val photosUris = viewModel.getPhotos(taskId)
                        //for (photo in photosUris.indices) {
                        uploadImage(photosUris)
                    //}

                } catch (e: Exception) {
                   // Toast.makeText(requireContext(), "Файл не найден", Toast.LENGTH_SHORT).show()
                   // Log.d("test", e.toString())
                }
            }
        }
    }



    companion object {
        const val PICK_XML_FILE = 1
    }

    override fun onItemClick(task: Task, position: Int) {
        val bundle = bundleOf(
            "taskId" to task.id, "fileName" to task.fileName, "name" to task.name,
            "info" to "Id завдання: ${task.id} , Записи: ${task.count}, Дата створення: ${task.date}, Юр.особи: ${task.isJur}"
        )
        findNavController().navigate(R.id.action_tasksFragment_to_taskDetailFragment, bundle)
    }

    override fun onLongClick(task: Task, position: Int) {
        createMenuDialog(task)
    }

    private fun createMenuDialog(task: Task) {
        val options = arrayOf<CharSequence>(
            getString(R.string.upload_task),
            getString(R.string.clear_field_btn),
            getString(R.string.delete_task),
            getString(R.string.cancel)
        )
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Виберіть дію:")
        builder.setItems(options) { dialog: DialogInterface, item: Int ->
            when {
                options[item] == getString(R.string.upload_task) -> {
                    createDoc(task)
                }
                options[item] == getString(R.string.clear_field_btn) -> {
                    clearTaskData(task)
                }
                options[item] == getString(R.string.delete_task) -> {
                    deleteTask(task)
                }
                else -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun prepareFilePart(partName: String, fileUri: Uri, body: UploadRequestBody): MultipartBody.Part {
        val file = File(fileUri.path)

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.name, body)
    }

    private fun uploadImage(uriStrings: List<String>) {

        //if (uri == null) {
        //    return
        //}

        var list: ArrayList<MultipartBody.Part> = ArrayList()
        for(i in uriStrings.indices) {

            val parcelFileDescriptor =
                requireActivity().contentResolver.openFileDescriptor(
                    uriStrings[i].toUri(),
                    "r",
                    null
                ) ?: return

            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
            val file = File(
                requireActivity().cacheDir, requireActivity().contentResolver.getFileName(
                    uriStrings[i].toUri()
                )
            )
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            val body = UploadRequestBody(file, "image", this)
            list.add(prepareFilePart("files", uriStrings[i].toUri(), body))

        }
        binding.progressBar.progress = 0

        MyApi().uploadImage(
            list,
            RequestBody.create(MediaType.parse("multipart/form-data"), "json")
        ).enqueue(object : Callback<UploadResponse> {
            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                binding.layoutRoot.snackbar(t.message!!)
                binding.progressBar.progress = 0
            }

            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                response.body()?.let {
                    binding.layoutRoot.snackbar(it.message)
                    binding.progressBar.progress = 100
                }
            }
        })

    }

    override fun onProgressUpdate(percentage: Int) {
        binding.progressBar.progress = percentage
    }

    private fun clearTaskData(task: Task) {
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    viewModel.clearTaskData(task.id)
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    dialog.dismiss()
                }
            }
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Ви впевнені, що хочете видалити збережені дані?").setPositiveButton(
            getString(R.string.yes),
            dialogClickListener
        )
                .setNegativeButton(getString(R.string.no), dialogClickListener).show()

    }

    private fun deleteTask(task: Task) {
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    viewModel.deleteTask(task.id)
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    dialog.dismiss()
                }
            }
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Ви впевнені, що хочете видалити це завдання?").setPositiveButton(
            getString(R.string.yes),
            dialogClickListener
        )
            .setNegativeButton(getString(R.string.no), dialogClickListener).show()
    }

    private fun createDoc(task: Task) {
        taskId = task.id
        val export = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/xml"
            putExtra(
                Intent.EXTRA_TITLE, "${task.fileName!!.split(".")[0].replace("E", "I")}_${
                    task.name.replace(
                        " ",
                        "_"
                    )
                }"
            )
        }
        startActivityForResult(export, 2)
    }

    private fun getAppVersion(context: Context): String {
        var version = ""
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return version
    }

    private fun showInfo() {

        AlertDialog.Builder(requireContext())
                //set icon
                .setIcon(android.R.drawable.ic_dialog_alert)
                //set title
                .setTitle(getString(R.string.app_info))
                //set message
                .setMessage(
                    "Додаток створено для контролерів АТ ПОЛТАВАОБЛЕНЕРГО\nРозробник: Громов Євгеній, тел.510-557\nВерсія: " + getAppVersion(
                        requireContext()
                    )
                )
                //set negative button
                .setNegativeButton("Oк") { _, _ ->
                    //set what should happen when negative button is clicked
                    //Toast.makeText(requireContext(), "Nothing Happened", Toast.LENGTH_LONG).show()
                }
                .show()
    }


}