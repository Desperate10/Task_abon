package ua.POE.Task_abon.presentation.finduser

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SimpleAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ua.POE.Task_abon.R
import ua.POE.Task_abon.databinding.FragmentFindUserBinding
import ua.POE.Task_abon.presentation.MainActivity
import ua.POE.Task_abon.utils.autoCleared


@AndroidEntryPoint
class FindUserFragment : Fragment(), AdapterView.OnItemSelectedListener, View.OnClickListener {

    private var binding: FragmentFindUserBinding by autoCleared()
    private val viewModel: FindUserViewModel by viewModels()

    private var adapter: ArrayAdapter<String>? = null
    private var existAdapter: ArrayAdapter<String>? = null
    private var simpleAdapter: SimpleAdapter? = null

    private var taskId = 0
    private var taskName: String? = null
    private var fileName: String? = null
    private var info: String? = null

    private var existFields = mutableListOf<String>()
    private var list = mutableListOf<Map<String, String>>()
    private var listHash = mutableMapOf<String, String>()
    private val searchFieldNames: MutableList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = "Критерії пошуку"

        readArguments()
        initSearchSpinner()
        initClickListeners()
        initAdapterForSearchCriteria()
    }

    override fun onDestroyView() {
        super.onDestroyView()
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

    private fun initAdapterForSearchCriteria() {
        val from = arrayOf("name", "value")
        val to = intArrayOf(R.id.name, R.id.value)
        simpleAdapter = SimpleAdapter(requireContext(), list, R.layout.search_item_row, from, to)
        binding.filterList.adapter = simpleAdapter
        binding.filterList.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, i, _ ->
                showDialog(i)
                true
            }

    }

    private fun showDialog(position: Int) {
        val options = arrayOf<CharSequence>("Видалити фильтр", "Відміна")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Виберіть дію:")
        builder.setItems(options) { dialog: DialogInterface, item: Int ->
            when {
                options[item] == "Видалити фільтр" -> {
                    list.removeAt(position)
                    simpleAdapter?.notifyDataSetChanged()
                }
                else -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.add_filter -> {

                var hash = mutableMapOf<String, String>()
                val name = binding.filterSpinner.selectedItem.toString()
                hash["name"] = name
                hash["value"] = binding.editFilterValue.text.toString()
                list.add(hash)
                listHash[name] = binding.editFilterValue.text.toString()
                searchFieldNames?.remove(name)
                simpleAdapter?.notifyDataSetChanged()
                adapter?.notifyDataSetChanged()
                binding.filterSpinner.setSelection(1)


            }
            R.id.do_filter -> {
                navigateToTaskDetailFragment()
            }
            R.id.clear_text -> {
                binding.editFilterValue.text.clear()
            }
        }
    }

    private fun navigateToTaskDetailFragment() {
        val bundle = bundleOf(
            "taskId" to taskId,
            "searchList" to listHash,
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
                requireActivity().onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val selectedItem = parent.getItemAtPosition(position).toString()
        when (parent.id) {
            R.id.filter_spinner -> {
                // CoroutineScope(Dispatchers.IO).launch {
                existFields = viewModel.getSearchFieldValues(taskId, selectedItem)
                existAdapter =
                    ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        existFields
                    )

                binding.existItemsSpinner.adapter = existAdapter
                binding.existItemsSpinner.onItemSelectedListener = this
            }
            R.id.exist_items_spinner -> {
                binding.editFilterValue.setText(selectedItem)
            }
        }
    }

    private fun initSearchSpinner() {
        val spinner = binding.filterSpinner
        val fieldNames = viewModel.getSearchFieldsTxt(taskId)
        adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            fieldNames
        )
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFindUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}


}