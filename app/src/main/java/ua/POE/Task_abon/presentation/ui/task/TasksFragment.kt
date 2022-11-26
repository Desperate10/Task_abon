package ua.POE.Task_abon.presentation.ui.task

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.POE.Task_abon.BuildConfig
import ua.POE.Task_abon.R
import ua.POE.Task_abon.databinding.FragmentTasksBinding
import ua.POE.Task_abon.domain.model.TaskInfo
import ua.POE.Task_abon.presentation.adapters.TaskListAdapter
import ua.POE.Task_abon.presentation.ui.task.dialog.ClearTaskDataDialogFragment
import ua.POE.Task_abon.presentation.ui.task.dialog.DeleteTaskDialogFragment
import ua.POE.Task_abon.presentation.ui.task.dialog.TaskClickMenuFragmentDialog
import ua.POE.Task_abon.utils.autoCleaned
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.Writer


/*TODO
* - завернуть в sealed чтение файла
 - createFragment from static with navigation
*/

@AndroidEntryPoint
class TasksFragment : Fragment(), TaskListAdapter.OnTaskClickListener {

    private var binding: FragmentTasksBinding by autoCleaned()
    private val viewModel: TaskViewModel by viewModels()
    private var adapter: TaskListAdapter by autoCleaned { TaskListAdapter(requireContext()) }
    private var taskId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        requestPermission()
        registerOnBackPressed()
    }

    private fun registerOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finishAffinity()
                requireActivity().finish()
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val linearLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.recyclerview.adapter = adapter
        adapter.onTaskClickListener = this
        binding.recyclerview.layoutManager = linearLayoutManager

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collectLatest { tasks ->
                    if (tasks.isNotEmpty()) {
                        adapter.submitList(tasks)
                        binding.noTasks.visibility = View.GONE
                        binding.recyclerview.visibility = View.VISIBLE
                    } else {
                        binding.noTasks.visibility = View.VISIBLE
                        binding.recyclerview.visibility = View.GONE
                    }
                }
            }
        }

        binding.choose.setOnClickListener {
            chooseFile()
        }

        setupClickMenuDialog()
        setupClearDataDialogListener()
        setupDeleteTaskDialogListener()

        /*viewModel.taskLoadingStatus.observe(viewLifecycleOwner, Observer {
            when (it.data) {
                Resource.Success -> {
                    Toast.makeText(activity, "Завдання успішно вигружено", Toast.LENGTH_SHORT).show()
                }
                Resource.Status.ERROR -> {
                    Toast.makeText(activity, "Помилка вигрузки завдання", Toast.LENGTH_SHORT).show()
                }
                Resource.Status.LOADING -> {
                    Toast.makeText(requireActivity(), "Вигрузка...", Toast.LENGTH_SHORT).show()
                }
            }
        })*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tasksmenu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_task -> {
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
        PermissionX.init(this)
            .permissions(
                Manifest.permission.READ_EXTERNAL_STORAGE
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
                if (!allGranted) {
                    Toast.makeText(
                        requireContext(),
                        "${getString(R.string.denied_permissions_text)} $deniedList",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onClick(task: TaskInfo) {
        navigateToTaskDetailFragment(task)
    }

    override fun onLongClick(task: TaskInfo) {
        TaskClickMenuFragmentDialog.show(parentFragmentManager, task)
    }

    private fun navigateToTaskDetailFragment(task: TaskInfo) {
        findNavController().navigate(
            TasksFragmentDirections.actionTasksFragmentToTaskDetailFragment(
                task,
                null
            )
        )
    }

    private fun setupClickMenuDialog() {
        TaskClickMenuFragmentDialog.setupListeners(
            parentFragmentManager,
            viewLifecycleOwner
        ) { taskInfo, which ->
            val task = Gson().fromJson<TaskInfo>(taskInfo, object : TypeToken<TaskInfo>() {}.type)
            when (which) {
                getString(R.string.upload_task) -> {
                    createDoc(task)
                }
                getString(R.string.clear_field_btn) -> {
                    ClearTaskDataDialogFragment.show(parentFragmentManager, task.id)
                }
                getString(R.string.delete_task) -> {
                    DeleteTaskDialogFragment.show(parentFragmentManager, task.id)
                }
            }
        }
    }

    private fun setupClearDataDialogListener() {
        ClearTaskDataDialogFragment.setupListeners(
            parentFragmentManager,
            viewLifecycleOwner
        ) { taskId ->
            viewModel.clearTaskData(taskId)
        }
    }

    private fun setupDeleteTaskDialogListener() {
        DeleteTaskDialogFragment.setupListeners(
            parentFragmentManager,
            viewLifecycleOwner
        ) { taskId ->
            viewModel.deleteTask(taskId)
        }
    }

    private fun uploadImages(taskId: Int) {
        viewModel.uploadImagesRequestBuilder(taskId)
    }

    private fun chooseFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/xml"
        }
        startPickXMLIntent.launch(intent)
    }

    private val startPickXMLIntent: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        it.data?.data?.also { uri -> viewModel.insert(uri) }
    }

    private fun createDoc(task: TaskInfo) {
        taskId = task.id
        val export = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/xml"
            putExtra(
                Intent.EXTRA_TITLE, "${task.fileName.split(".")[0].replace("E", "I")}_${
                    task.name.replace(
                        " ", "_"
                    )
                }"
            )
        }
        startCreateXMLIntent.launch(export)
    }

    private val startCreateXMLIntent: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val uri = it?.data?.data
            try {
                val os = uri?.let { requireActivity().contentResolver.openOutputStream(it) }
                withContext(Dispatchers.IO) {
                    val w: Writer = BufferedWriter(OutputStreamWriter(os, "windows-1251"))
                    val sb = viewModel.createXml(taskId)
                    w.write(sb)
                    w.flush()
                    w.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        uploadImages(taskId)
    }

    private fun showInfo() {
        AlertDialog.Builder(requireContext()).setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(getString(R.string.app_info)).setMessage(
                "Додаток створено для контролерів АТ ПОЛТАВАОБЛЕНЕРГО\n" +
                        "Розробник: Громов Євгеній, тел.510-557\n" +
                        "Версія: ${BuildConfig.VERSION_NAME}"
            ).setNegativeButton("Oк") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

}