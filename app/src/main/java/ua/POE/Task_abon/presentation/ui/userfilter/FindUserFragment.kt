package ua.POE.Task_abon.presentation.ui.userfilter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SimpleAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ua.POE.Task_abon.R
import ua.POE.Task_abon.databinding.FragmentFindUserBinding
import ua.POE.Task_abon.presentation.model.SearchMap
import ua.POE.Task_abon.presentation.ui.userfilter.dialog.DeleteSearchFilterDialogFragment
import ua.POE.Task_abon.utils.autoCleaned
import ua.POE.Task_abon.utils.onItemSelected


@AndroidEntryPoint
class FindUserFragment : Fragment(), View.OnClickListener {

    private val args by navArgs<FindUserFragmentArgs>()
    private var binding: FragmentFindUserBinding by autoCleaned()
    private val viewModel: FindUserViewModel by viewModels()

    private var adapter: ArrayAdapter<String>? = null
    private var simpleAdapter: SimpleAdapter? = null

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

    private fun initClickListeners() {
        binding.addFilter.setOnClickListener(this)
        binding.doFilter.setOnClickListener(this)
        binding.clearText.setOnClickListener(this)
    }

    private fun initAdapterForSearchCriteria(filterList: List<Map<String, String>>) {
        val from = arrayOf("name", "value")
        val to = intArrayOf(R.id.name, R.id.value)
        simpleAdapter =
            SimpleAdapter(requireContext(), filterList, R.layout.search_item_row, from, to)
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
                showFilteredUsers()
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

    private fun updateFilterList(filter: Map<String, String>) {
        list.add(filter)
        viewModel.updateFilter(list)
    }

    private fun showFilteredUsers() {
        Log.d("testim", searchListHash.toString())
        findNavController().navigate(
            FindUserFragmentDirections.actionFindUserFragmentToTaskDetailFragment(
                args.task,
                SearchMap(searchListHash)
            )
        )
    }

    private fun navigateToTaskDetailFragment() {
        findNavController().popBackStack()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigateToTaskDetailFragment()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initExistAdapter(fieldValues: List<String>) {
        val existAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                fieldValues
            )
        binding.existItemsSpinner.adapter = existAdapter
        binding.existItemsSpinner.onItemSelected { parent, position ->
            binding.editFilterValue.setText(parent?.getItemAtPosition(position).toString())
        }
    }

    private fun initSearchSpinner(fieldNames: List<String>) {
        adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            fieldNames
        )
        binding.filterSpinner.adapter = adapter
        binding.filterSpinner.onItemSelected { parent, position ->
            viewModel.getSearchFieldValues(parent?.getItemAtPosition(position).toString())
        }
    }
}