package ua.POE.Task_abon.presentation.taskdetail

import android.app.AlertDialog
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.Scroller
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import ua.POE.Task_abon.R
import ua.POE.Task_abon.data.entities.UserData
import ua.POE.Task_abon.databinding.FragmentTaskDetailBinding
import ua.POE.Task_abon.domain.model.Icons
import ua.POE.Task_abon.presentation.MainActivity
import ua.POE.Task_abon.presentation.adapters.CustomerListAdapter
import ua.POE.Task_abon.utils.*


@AndroidEntryPoint
class TaskDetailFragment : Fragment(), CustomerListAdapter.OnCustomerClickListener {

    private var binding: FragmentTaskDetailBinding by autoCleaned()
    private val viewModel by viewModels<TaskDetailViewModel>()
    private var adapter: CustomerListAdapter by autoCleaned()

    private var taskId = 0
    private var searchList: Map<String, String>? = null
    private var fileName: String? = null
    private var info: String? = null
    private var taskName: String? = null

    private var userData = listOf<UserData>()
    private var icons = ArrayList<Icons>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).supportActionBar?.title = "Список абонентів"
        hideKeyboard()
        readBundle()
        bindViews()
        createCustomerListAdapter()
        observeViewModel()
        addClickListeners()
    }

    private fun addClickListeners() {
        binding.isDoneCheckBox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.customersFilterStatus.value = if (isChecked) NOT_FINISHED
            else ALL
        }
        adapter.onCustomerClickListener = this
    }

    private fun createCustomerListAdapter() {
        //read all icons from raw
        icons = resources.getRawTextFile(R.raw.icons)
        val linearLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.recyclerview.layoutManager = linearLayoutManager
        adapter = CustomerListAdapter(requireContext(), icons)
        binding.recyclerview.adapter = adapter
    }

    private fun bindViews() {
        binding.taskName.text = taskName
        binding.fileName.text = fileName
        binding.info.text = info
    }

    private fun readBundle() {
        arguments?.let {
            taskId = it.getInt("taskId")
            searchList = it.get("searchList") as Map<String, String>?
            taskName = it.getString("taskName")
            fileName = it.getString("fileName")
            info = it.getString("info")
        }
    }

    private fun observeViewModel() {
        viewModel.customersFilterStatus.observe(viewLifecycleOwner) { status ->
            userData = if (status == ALL) {
                viewModel.getUsers(taskId, searchList)
            } else {
                viewModel.getUsersByStatus("TD$taskId", status)
            }
            adapter.submitList(userData)
            with(binding.recyclerview) {
                post { scrollToPosition(0) }
            }
        }
        viewModel.getFinishedCount(taskId).observe(viewLifecycleOwner) { count ->
            binding.finished.text = getString(R.string.status_done, count, userData.size)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigate(R.id.action_taskDetailFragment_to_tasksFragment)
            }
            R.id.find_user -> {
                navigateToFindUserFragment()
            }
            R.id.marks -> {
                showEmojiInfoDialog()
            }
        }
        return super.onOptionsItemSelected(item)
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

    private fun showEmojiInfoDialog() {

        val message =
            icons.joinToString(separator = "") { "${it.emoji?.let { it1 -> getEmojiByUnicode(it1) }} - ${it.hint}\n" }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Умовні позначки")
            .setMessage(message)
            .setPositiveButton("Ок") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()

        val textView = dialog.findViewById(android.R.id.message) as TextView
        textView.setScroller(Scroller(requireContext()))
        textView.isVerticalScrollBarEnabled = true
        textView.movementMethod = ScrollingMovementMethod()
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
        private const val NOT_FINISHED = "Не виконано"
        private const val ALL = "Всі"
        private const val FIRST_FILIAL_NUMBER = 1
        private const val LAST_FILIAL_NUMBER = 5
    }
}