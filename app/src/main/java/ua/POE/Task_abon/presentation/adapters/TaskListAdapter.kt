package ua.POE.Task_abon.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ua.POE.Task_abon.R
import ua.POE.Task_abon.databinding.RowTaskBinding
import ua.POE.Task_abon.presentation.model.Task
import javax.inject.Inject

/**
 * RecyclerViewAdapter for displaying list of tasks on TaskFragment
 * */
class TaskListAdapter @Inject constructor(val context: Context) :
    ListAdapter<Task, TaskListViewHolder>(TaskListDiffUtil) {

    var onTaskClickListener: OnTaskClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListViewHolder {
        val itemBinding = RowTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(
        holder: TaskListViewHolder,
        position: Int
    ) {
        val task = getItem(position)
        with(holder.binding) {
            val infoTemplate = context.getString(R.string.info_template)
            taskName.text = task.name
            fileName.text = task.fileName
            info.text = String.format(infoTemplate, task.id, task.userCount, task.date, task.isJur)
            root.setOnClickListener {
                onTaskClickListener?.onClick(task)
            }
            root.setOnLongClickListener {
                onTaskClickListener?.onLongClick(task)
                true
            }
        }
    }

    interface OnTaskClickListener {
        fun onClick(task: Task)
        fun onLongClick(task: Task)
    }
}