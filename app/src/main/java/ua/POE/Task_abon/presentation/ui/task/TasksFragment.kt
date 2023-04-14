package ua.POE.Task_abon.presentation.ui.task

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ua.POE.Task_abon.BuildConfig
import ua.POE.Task_abon.R
import ua.POE.Task_abon.databinding.FragmentTasksBinding
import ua.POE.Task_abon.presentation.model.Task
import ua.POE.Task_abon.presentation.adapters.TaskListAdapter
import ua.POE.Task_abon.presentation.ui.task.dialog.ClearTaskDataDialogFragment
import ua.POE.Task_abon.presentation.ui.task.dialog.DeleteTaskDialogFragment
import ua.POE.Task_abon.presentation.ui.task.dialog.TaskClickMenuFragmentDialog
import ua.POE.Task_abon.utils.autoCleaned
import ua.POE.Task_abon.utils.snackbar

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
        bindViews()
        observeViewModel()
        setupClickListeners()
        setupClickMenuDialog()
        setupClearDataDialogListener()
        setupDeleteTaskDialogListener()
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
            android.R.id.home -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
                true
            }
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

    private fun bindViews() {
        val linearLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = linearLayoutManager
        adapter.onTaskClickListener = this
    }

    private fun setupClickListeners() {
        binding.choose.setOnClickListener {
            chooseFile()
        }
    }

    private fun observeViewModel() {
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
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.createXmlState.collectLatest {
                    binding.rootLayout.snackbar(it)
                }
            }
        }
    }

    @SuppressLint("InlinedApi")
    private fun requestPermission() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.POST_NOTIFICATIONS
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

    override fun onClick(task: Task) {
        navigateToTaskDetailFragment(task)
    }

    override fun onLongClick(task: Task) {
        TaskClickMenuFragmentDialog.show(parentFragmentManager, task)
    }

    private fun navigateToTaskDetailFragment(task: Task) {
        if (findNavController().currentDestination?.id == R.id.tasksFragment) {
            findNavController().navigate(
                TasksFragmentDirections.actionTasksFragmentToTaskDetailFragment(
                    task,
                    null
                )
            )
        }
    }

    private fun setupClickMenuDialog() {
        TaskClickMenuFragmentDialog.setupListeners(
            parentFragmentManager,
            viewLifecycleOwner
        ) { taskInfo, which ->
            val task = Gson().fromJson<Task>(taskInfo, object : TypeToken<Task>() {}.type)
            when (which) {
                getString(R.string.upload_task) -> {
                    createXmlDocument(task)
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

    private fun createXmlDocument(task: Task) {
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
        val uri = it?.data?.data
        viewModel.createXml(taskId, uri)
    }

    private fun showInfo() {
        AlertDialog.Builder(requireContext()).setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(getString(R.string.app_info)).setMessage(
                String.format(
                    getString(R.string.app_info_text_template), BuildConfig.VERSION_NAME
                )
            )
            .setNegativeButton("Oк") { dialog, _ -> dialog.dismiss() }
            .show()
    }

}