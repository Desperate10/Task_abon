package ua.POE.Task_abon.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import ua.POE.Task_abon.presentation.model.Task

object TaskListDiffUtil: DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}