package ua.POE.Task_abon.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import ua.POE.Task_abon.presentation.model.TaskInfo

object TaskListDiffUtil: DiffUtil.ItemCallback<TaskInfo>() {
    override fun areItemsTheSame(oldItem: TaskInfo, newItem: TaskInfo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TaskInfo, newItem: TaskInfo): Boolean {
        return oldItem == newItem
    }
}