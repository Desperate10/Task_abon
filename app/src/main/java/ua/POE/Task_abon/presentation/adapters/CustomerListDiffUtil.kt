package ua.POE.Task_abon.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import ua.POE.Task_abon.data.entities.UserData

object CustomerListDiffUtil : DiffUtil.ItemCallback<UserData>() {

    override fun areItemsTheSame(oldItem: UserData, newItem: UserData): Boolean {
        return oldItem._id == newItem._id
    }

    override fun areContentsTheSame(oldItem: UserData, newItem: UserData): Boolean {
        return oldItem == newItem
    }
}