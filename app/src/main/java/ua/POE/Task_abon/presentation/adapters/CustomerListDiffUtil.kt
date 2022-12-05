package ua.POE.Task_abon.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import ua.POE.Task_abon.presentation.model.CustomerMainData

object CustomerListDiffUtil : DiffUtil.ItemCallback<CustomerMainData>() {

    override fun areItemsTheSame(oldItem: CustomerMainData, newItem: CustomerMainData): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CustomerMainData, newItem: CustomerMainData): Boolean {
        return oldItem == newItem
    }
}