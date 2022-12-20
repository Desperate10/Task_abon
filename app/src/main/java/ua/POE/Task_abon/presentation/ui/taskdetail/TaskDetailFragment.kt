package ua.POE.Task_abon.presentation.ui.taskdetail

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ua.POE.Task_abon.R
import ua.POE.Task_abon.presentation.model.CustomerMainData
import ua.POE.Task_abon.databinding.FragmentTaskDetailBinding
import ua.POE.Task_abon.presentation.adapters.CustomerListAdapter
import ua.POE.Task_abon.presentation.ui.taskdetail.dialog.IconsHelpDialogFragment
import ua.POE.Task_abon.utils.autoCleaned
import ua.POE.Task_abon.utils.hideKeyboard


@AndroidEntryPoint
class TaskDetailFragment : Fragment(), CustomerListAdapter.OnCustomerClickListener {

    private val args by navArgs<TaskDetailFragmentArgs>()

    private var binding: FragmentTaskDetailBinding by autoCleaned()
    private val viewModel by viewModels<TaskDetailViewModel>()
    private var adapter: CustomerListAdapter by autoCleaned()
    private var count = 0

    private var userData = listOf<CustomerMainData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        hideKeyboard()
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
        binding.taskName.text = args.task.name
        binding.fileName.text = args.task.fileName
        binding.info.text = String.format(
            getString(R.string.info_template),
            args.task.id,
            args.task.userCount,
            args.task.date,
            args.task.isJur
        )
    }

    private fun observeViewModel() {
        viewModel.setCustomerStatus(false)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getCustomersData.collect {
                    userData = it
                    adapter.submitList(it)
                    binding.finished.text =
                        getString(R.string.status_done, count, userData.size)
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

    private fun navigateToTaskFragment() {
        findNavController().popBackStack()
    }

    private fun navigateToFindUserFragment() {
        if (findNavController().currentDestination?.id == R.id.taskDetailFragment) {
            findNavController().navigate(
                TaskDetailFragmentDirections.actionTaskDetailFragmentToFindUserFragment(
                    args.task
                )
            )
        }
    }

    private fun navigateToUserInfoFragment(position: Int) {
        if (findNavController().currentDestination?.id == R.id.taskDetailFragment) {
            findNavController().navigate(
                TaskDetailFragmentDirections.actionTaskDetailFragmentToUserInfoFragment(
                    args.task.id,
                    args.task.filial,
                    userData[position].id,
                    adapter.itemCount
                )
            )
        }
    }
}