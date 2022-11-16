package ua.POE.Task_abon.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ua.POE.Task_abon.R
import ua.POE.Task_abon.databinding.RowTaskBinding
import ua.POE.Task_abon.domain.model.TaskInfo
import javax.inject.Inject

class TaskListAdapter @Inject constructor(val context: Context) :
    ListAdapter<TaskInfo, TaskListViewHolder>(TaskListDiffUtil) {

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
            info.text = String.format(infoTemplate, task.id, task.count, task.date, task.isJur)
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
        fun onClick(task: TaskInfo)
        fun onLongClick(task: TaskInfo)
    }
}