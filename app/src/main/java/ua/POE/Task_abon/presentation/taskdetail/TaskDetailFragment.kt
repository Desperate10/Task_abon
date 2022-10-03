package ua.POE.Task_abon.presentation.taskdetail

import android.annotation.SuppressLint
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
import ua.POE.Task_abon.presentation.MainActivity
import ua.POE.Task_abon.utils.*


@AndroidEntryPoint
class TaskDetailFragment : Fragment(), PersonListAdapter.ItemCLickListener {

    private var binding: FragmentTaskDetailBinding by autoCleared()
    private val viewModel by viewModels<TaskDetailViewModel>()
    private var adapter: PersonListAdapter by autoCleared()

    private var taskId: String? = null
    private var searchList = mutableMapOf<String, String>()
    private var userData = listOf<UserData>()

    private var icons = mutableListOf<Icons>()
    private var userStatus = NOT_DONE

    companion object {
        private const val NOT_DONE = "Не виконано"
        private const val DONE = "Виконано"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = "Список абонентів"
        hideKeyboard()

        val linearLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.recyclerview.layoutManager = linearLayoutManager
        taskId = arguments?.getString("taskId")
        searchList = arguments?.get("searchList") as MutableMap<String, String>

        binding.taskName.text = arguments?.getString("name")
        binding.fileName.text = arguments?.getString("fileName")
        binding.info.text = arguments?.getString("info")

        icons = resources.getRawTextFile(R.raw.icons)

        observeViewModel()

        //заменить на ливдату из вьюмодели
        binding.isDoneCheckBox.setOnCheckedChangeListener { _, isChecked ->
            userStatus = if (isChecked) NOT_DONE
            else DONE
        }
    }

    private fun observeViewModel() {
        if (searchList.isNullOrEmpty()) {
            viewModel.getUsers("TD$taskId").observe(viewLifecycleOwner) { list ->
                userData = list
                taskId?.let {
                    viewModel.getFinishedCount(it).observe(viewLifecycleOwner) { count ->
                        binding.finished.text = "Виконано: $count/${userData.size} записів"
                    }
                }
            }
        } else {
            viewModel.getSearchedUsers("$taskId", searchList).observe(viewLifecycleOwner) { list ->
                userData = list
                taskId?.let {
                    viewModel.getFinishedCount(it).observe(viewLifecycleOwner) { count ->
                        binding.finished.text = buildString {
                            append("Виконано: ")
                            append(count)
                            append("/")
                            append(userData.size)
                            append(" записів")
                        }
                    }
                }
            }
        }
        adapter = PersonListAdapter(userData, icons, this)
        binding.recyclerview.adapter = adapter
        adapter.notifyDataSetChanged()

        viewModel.getUsersByStatus("TD$taskId", userStatus).observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
            userData = list
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigate(R.id.tasksFragment)
                return true
            }
            R.id.find_user -> {
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
                return true
            }
            R.id.marks -> {
                showEmojiInfoDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showEmojiInfoDialog() {
        var message = ""

        icons.forEach {
            message += getEmojiByUnicode(it.emoji) + " - " + it.hint + "\n"
        }

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
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onItemClick(position: Int) {
        //Log.d("userData", userData[position]._id.toString())
        val bundle = bundleOf(
            "taskId" to taskId,
            "filial" to binding.fileName.text.substring(1, 5),
            "num" to userData[position].num,
            "id" to userData[position]._id,
            "count" to binding.recyclerview.adapter?.itemCount,
            "isFirstLoad" to true
        )
        findNavController().navigate(R.id.action_taskDetailFragment_to_userInfoFragment, bundle)
    }
}