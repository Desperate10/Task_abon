package ua.POE.Task_abon.presentation.taskdetail

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ua.POE.Task_abon.R
import ua.POE.Task_abon.data.entities.UserData
import ua.POE.Task_abon.databinding.RowPersonBinding
import ua.POE.Task_abon.domain.model.Icons
import ua.POE.Task_abon.utils.getEmojiByUnicode
import javax.inject.Inject

class CustomerListAdapter @Inject constructor(
    val context: Context,
    private var personList: List<UserData>,
    private var emojiList: List<Icons>,
    val mItemClickListener: ItemCLickListener
) : RecyclerView.Adapter<CustomerListAdapter.PersonListViewHolder>() {

    interface ItemCLickListener {
        fun onItemClick(position: Int)
    }

    inner class PersonListViewHolder(private val binding: RowPersonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(person: UserData) {
            binding.number.text = person.num
            binding.accountNumber.text = person.numbpers
            if (!person.icons_account.isNullOrBlank()) {
                val accountIconsList = person.icons_account.split("/", "\\")

                val emojis =
                    emojiList.filter { it.id in accountIconsList }
                        .joinToString { getEmojiByUnicode(it.emoji) }

                binding.accountNumber.text = context.getString(
                    R.string.account_number,
                    "${binding.accountNumber.text}  ",
                    emojis
                )
            }
            binding.name.text = person.family
            binding.adress.text = person.address
            binding.pillar.text = context.getString(
                R.string.pillar,//person.fider +" Опора: " +person.opora
                person.fider,
                person.opora
            )
            binding.counter.text = "Лічильник: " + person.counterNumb
            if (!person.icons_counter.isNullOrBlank()) {
                val counterIconsList = person.icons_counter.split("/")

                val emojis =
                    emojiList.filter { it.id in counterIconsList }
                        .joinToString { getEmojiByUnicode(it.emoji) }

                binding.counter.text = context.getString(
                    R.string.account_number,
                    "${binding.counter.text}  ",
                    emojis
                )
            }
            binding.isDone.text = person.done
        }

        init {
            binding.root.setOnClickListener {
                mItemClickListener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonListViewHolder {
        val itemBinding =
            RowPersonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PersonListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: PersonListViewHolder, position: Int) {
        val person: UserData = personList[position]
        holder.bind(person)

    }

    override fun getItemCount(): Int = personList.size

    fun updateList(list: List<UserData>) {
        personList = list
        notifyDataSetChanged()
    }
}