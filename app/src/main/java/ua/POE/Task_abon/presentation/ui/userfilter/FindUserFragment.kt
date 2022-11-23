package ua.POE.Task_abon.presentation.ui.userfilter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SimpleAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ua.POE.Task_abon.R
import ua.POE.Task_abon.databinding.FragmentFindUserBinding
import ua.POE.Task_abon.presentation.ui.userfilter.dialog.DeleteSearchFilterDialogFragment
import ua.POE.Task_abon.presentation.ui.userinfo.listener.ItemSelectedListener
import ua.POE.Task_abon.utils.autoCleaned


@AndroidEntryPoint
class FindUserFragment : Fragment(), ItemSelectedListener, View.OnClickListener {

    private var binding : FragmentFindUserBinding by autoCleaned()
    private val viewModel: FindUserViewModel by viewModels()

    private var adapter: ArrayAdapter<String>? = null
    private var simpleAdapter: SimpleAdapter? = null

    private var taskId = 0
    private var taskName: String? = null
    private var fileName: String? = null
    private var info: String? = null

    private var list = mutableListOf<Map<String, String>>()
    private var searchListHash = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFindUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        readArguments()
        setupFilterClickListener()
        setupDeleteFilterListener()
        initClickListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchFields.collectLatest {
                    initSearchSpinner(it)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchFieldsValues.collectLatest {
                    initExistAdapter(it)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filterHashList.collectLatest {
                    initAdapterForSearchCriteria(it)
                }
            }
        }
    }

    private fun readArguments() {
        taskId = arguments?.getInt("taskId")
            ?: throw NullPointerException("taskId is null in FindUserFragment")
        taskName = arguments?.getString("name")
            ?: throw NullPointerException("taskName is null in FindUserFragment")
        fileName = arguments?.getString("fileName")
            ?: throw NullPointerException("fileName's null in FindUserFragment")
        info = arguments?.getString("info")
            ?: throw NullPointerException("info is null in FindUserFragment")
    }

    private fun initClickListeners() {
        binding.addFilter.setOnClickListener(this)
        binding.doFilter.setOnClickListener(this)
        binding.clearText.setOnClickListener(this)
    }

    private fun initAdapterForSearchCriteria(filterList: List<Map<String,String>>) {
        val from = arrayOf("name", "value")
        val to = intArrayOf(R.id.name, R.id.value)
        simpleAdapter = SimpleAdapter(requireContext(), filterList, R.layout.search_item_row, from, to)
        binding.filterList.adapter = simpleAdapter
    }

    private fun setupFilterClickListener() {
        binding.filterList.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, position, _ ->
                DeleteSearchFilterDialogFragment.show(parentFragmentManager, position)
                true
            }
    }

    private fun setupDeleteFilterListener() {
        DeleteSearchFilterDialogFragment.setupListener(parentFragmentManager, viewLifecycleOwner) {
            deleteFilter(it)
        }
    }

    private fun deleteFilter(position: Int) {
        list.removeAt(position)
        viewModel.updateFilter(list)
        simpleAdapter?.notifyDataSetChanged()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.add_filter -> {
                addFilterCriteria()
            }
            R.id.do_filter -> {
                navigateToTaskDetailFragment()
            }
            R.id.clear_text -> {
                binding.editFilterValue.text.clear()
            }
        }
    }

    private fun addFilterCriteria() {
        val filter = mutableMapOf<String, String>()
        val name = binding.filterSpinner.selectedItem.toString()
        val value = binding.editFilterValue.text.toString()
        filter["name"] = name
        filter["value"] = value
        updateFilterList(filter)
        searchListHash[name] = value
        simpleAdapter?.notifyDataSetChanged()
        adapter?.notifyDataSetChanged()
        binding.filterSpinner.setSelection(1)
    }

    private fun updateFilterList(filter: Map<String,String>) {
        list.add(filter)
        viewModel.updateFilter(list)
    }

    private fun navigateToTaskDetailFragment() {
        val bundle = bundleOf(
            "taskId" to taskId,
            "taskName" to taskName,
            "searchList" to searchListHash,
            "fileName" to fileName,
            "name" to taskName,
            "info" to info
        )
        findNavController().navigate(
            R.id.action_findUserFragment_to_taskDetailFragment,
            bundle
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val selectedItem = parent.getItemAtPosition(position).toString()
        when (parent.id) {
            R.id.filter_spinner -> {
                viewModel.getSearchFieldValues(selectedItem)
            }
            R.id.exist_items_spinner -> {
                binding.editFilterValue.setText(selectedItem)
            }
        }
    }
    private fun initExistAdapter(fieldValues: List<String>) {
        val existAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                fieldValues
            )
        binding.existItemsSpinner.adapter = existAdapter
        binding.existItemsSpinner.onItemSelectedListener = this
    }

    private fun initSearchSpinner(fieldNames: List<String>) {
        adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            fieldNames
        )
        binding.filterSpinner.adapter = adapter
        binding.filterSpinner.onItemSelectedListener = this
    }
}