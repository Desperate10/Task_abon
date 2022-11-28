package ua.POE.Task_abon.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import ua.POE.Task_abon.data.entities.UserDataEntity

object CustomerListDiffUtil : DiffUtil.ItemCallback<UserDataEntity>() {

    override fun areItemsTheSame(oldItem: UserDataEntity, newItem: UserDataEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UserDataEntity, newItem: UserDataEntity): Boolean {
        return oldItem == newItem
    }
}