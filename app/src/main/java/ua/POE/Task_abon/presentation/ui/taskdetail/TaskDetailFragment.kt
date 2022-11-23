package ua.POE.Task_abon.presentation.ui.taskdetail

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ua.POE.Task_abon.R
import ua.POE.Task_abon.data.entities.UserData
import ua.POE.Task_abon.databinding.FragmentTaskDetailBinding
import ua.POE.Task_abon.presentation.adapters.CustomerListAdapter
import ua.POE.Task_abon.presentation.ui.taskdetail.dialog.IconsHelpDialogFragment
import ua.POE.Task_abon.utils.autoCleaned
import ua.POE.Task_abon.utils.hideKeyboard


@AndroidEntryPoint
class TaskDetailFragment : Fragment(), CustomerListAdapter.OnCustomerClickListener {

    private var binding: FragmentTaskDetailBinding by autoCleaned()
    private val viewModel by viewModels<TaskDetailViewModel>()
    private var adapter: CustomerListAdapter by autoCleaned()

    private var taskId = 0
    private var fileName: String? = null
    private var info: String? = null
    private var taskName: String? = null
    private var count = 0

    private var userData = listOf<UserData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        hideKeyboard()
        readArguments()
        bindViews()
        createCustomerListAdapter()
        observeViewModel()
        addClickListeners()
    }

    private fun addClickListeners() {
        binding.isDoneCheckBox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setCustomerStatus(isChecked)
        }
        adapter.onCustomerClickListener = this
    }

    private fun createCustomerListAdapter() {
        val linearLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.recyclerview.layoutManager = linearLayoutManager
        adapter = CustomerListAdapter(requireContext())
        binding.recyclerview.adapter = adapter
    }

    private fun bindViews() {
        binding.taskName.text = taskName
        binding.fileName.text = fileName
        binding.info.text = info
    }

    private fun readArguments() {
        arguments?.let {
            taskId = it.getInt("taskId")
            taskName = it.getString("taskName")
            fileName = it.getString("fileName")
            info = it.getString("info")
        }
    }

    private fun observeViewModel() {
        viewModel.setCustomerStatus(false)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getCustomersData.collect {
                    userData = it
                    adapter.submitList(it)
                    binding.finished.text = getString(R.string.status_done, count, userData.size)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.finishedCustomersCount.collectLatest {
                    count = it
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigateToTaskFragment()
            }
            R.id.find_user -> {
                navigateToFindUserFragment()
            }
            R.id.reset_filter -> {
                binding.isDoneCheckBox.isChecked = false
                viewModel.resetFilter()
            }
            R.id.marks -> {
                showIconsHelpHint()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToTaskFragment() {
        findNavController().navigate(R.id.action_taskDetailFragment_to_tasksFragment)
    }

    private fun navigateToFindUserFragment() {
        val bundle = bundleOf(
            "taskId" to taskId,
            "fileName" to binding.fileName.text.toString(),
            "name" to binding.taskName.text.toString(),
            "info" to binding.info.text
        )
        findNavController().navigate(
            R.id.action_taskDetailFragment_to_findUserFragment,
            bundle
        )
    }

    private fun showIconsHelpHint() {
        IconsHelpDialogFragment.show(parentFragmentManager)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.task_detail_menu, menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCustomerClick(position: Int) {
        navigateToUserInfoFragment(position)
    }

    private fun navigateToUserInfoFragment(position: Int) {
        val bundle = bundleOf(
            "taskId" to taskId,
            "filial" to extractFilialFromFileName(binding.fileName.text.toString()),
            "num" to userData[position].num,
            "id" to userData[position]._id,
            "count" to adapter.itemCount,
            "isFirstLoad" to true
        )
        findNavController().navigate(R.id.action_taskDetailFragment_to_userInfoFragment, bundle)
    }

    private fun extractFilialFromFileName(fileName: String): String {
        return fileName.substring(FIRST_FILIAL_NUMBER, LAST_FILIAL_NUMBER)
    }

    companion object {
        private const val FIRST_FILIAL_NUMBER = 1
        private const val LAST_FILIAL_NUMBER = 5
    }
}