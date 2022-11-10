package ua.POE.Task_abon.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ua.POE.Task_abon.R
import ua.POE.Task_abon.data.entities.UserData
import ua.POE.Task_abon.databinding.RowPersonBinding
import ua.POE.Task_abon.utils.getIcons
import ua.POE.Task_abon.utils.getNeededEmojis
import javax.inject.Inject

class CustomerListAdapter @Inject constructor(
    private val context: Context,
) : ListAdapter<UserData, CustomerListViewHolder>(CustomerListDiffUtil) {

    private val iconsList = context.getIcons()

    var onCustomerClickListener: OnCustomerClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerListViewHolder {
        val binding =
            RowPersonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomerListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerListViewHolder, position: Int) {
        val person = getItem(position)
        with(holder.binding) {
            with(person) {
                val accountAndCounterFinalTemplate =
                    context.resources.getString(R.string.account_counter_template)
                val pillarTemplate = context.resources.getString(R.string.pillar_template)
                val accountTemplate = context.resources.getString(R.string.account_template)
                val counterTemplate = context.resources.getString(R.string.counter_template)
                index.text = num.toString()
                accountNumber.text = String.format(accountTemplate, numbpers)
                if (!icons_account.isNullOrBlank()) {
                    val emojis = getNeededEmojis(iconsList, icons_account)

                    accountNumber.text = String.format(
                        accountAndCounterFinalTemplate,
                        accountNumber.text,
                        emojis
                    )
                }
                name.text = family
                adress.text = address
                pillar.text = String.format(
                    pillarTemplate,
                    person.fider,
                    person.opora
                )
                counter.text = String.format(counterTemplate, counterNumb)
                if (!icons_counter.isNullOrBlank()) {
                    val emojis = getNeededEmojis(iconsList, icons_counter)

                    counter.text =
                        String.format(accountAndCounterFinalTemplate, counter.text, emojis)
                }
                isDone.text = person.done
                root.setOnClickListener {
                    onCustomerClickListener?.onCustomerClick(position)
                }
            }
        }

    }

    interface OnCustomerClickListener {
        fun onCustomerClick(position: Int)
    }


}