package ua.POE.Task_abon.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ua.POE.Task_abon.data.entities.Task
import ua.POE.Task_abon.databinding.RowTaskBinding
import ua.POE.Task_abon.ui.tasks.TasksFragment
import javax.inject.Inject

class TaskListAdapter @Inject constructor(private val taskList: List<Task>, val mItemClickListener: TasksFragment) : RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder>() {

    interface ItemCLickListener{
        fun onItemClick(task: Task, position: Int)
        fun onLongClick(task: Task, position: Int)
    }

    inner class TaskListViewHolder(private val binding: RowTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.taskName.text = task.name
            binding.fileName.text = task.fileName
            binding.info.text = "Id завдання: ${task.id} , Записи: ${task.count}, Дата створення: ${task.date}, Юр.особи: ${task.isJur}"
        }

        init {
            binding.root.setOnClickListener {
                mItemClickListener.onItemClick(taskList[adapterPosition], adapterPosition)
            }
                binding.root.setOnLongClickListener{
                    mItemClickListener.onLongClick(taskList[adapterPosition], adapterPosition)
                    return@setOnLongClickListener true
                }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListViewHolder {
        val itemBinding = RowTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: TaskListViewHolder, position: Int) {
        val task : Task = taskList[position]
        holder.bind(task)

    }

    override fun getItemCount(): Int = taskList.size
}