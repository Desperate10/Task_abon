package ua.POE.Task_abon.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ua.POE.Task_abon.data.entities.UserData
import ua.POE.Task_abon.databinding.RowPersonBinding
import javax.inject.Inject

class PersonListAdapter @Inject constructor(private var personList : List<UserData>, private var emojiList: List<Icons>, val mItemClickListener:ItemCLickListener) : RecyclerView.Adapter<PersonListAdapter.PersonListViewHolder>() {

    interface ItemCLickListener{
        fun onItemClick(position: Int)
    }

    inner class PersonListViewHolder(private val binding: RowPersonBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(person: UserData) {
            binding.textView.text = person.num
            binding.text1.text = person.numbpers
            if (!person.icons_account.isNullOrBlank()) {
                val mods = person.icons_account.split("/")
                var i = 0
                binding.text1.text = binding.text1.text as String + "   "
                do {
                    binding.text1.text = binding.text1.text as String + getEmojiByUnicode(emojiList[mods[i].toInt()-1].emoji)
                    i++
                } while (i < mods.size)
            }
            binding.text2.text = person.family
            binding.text3.text = person.address
            binding.text4.text = person.fider +" Опора: " +person.opora
            binding.text5.text = "Лічильник: "+person.counterNumb
            if (!person.icons_counter.isNullOrBlank()) {
                val mods = person.icons_counter.split("/")
                var i = 0
                binding.text5.text = binding.text5.text as String + "   "
                do {
                    binding.text5.text = binding.text5.text as String + getEmojiByUnicode(emojiList[mods[i].toInt()-1].emoji!!)
                    i++
                } while (i < mods.size)
            }
           // if(person.isDone == "Выполнено") {
                binding.isDone.text =  person.done
            //} else {
             //   binding.isDone.text = ""
            //}
        }

        init {
            binding.root.setOnClickListener {
                mItemClickListener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonListViewHolder {
        val itemBinding = RowPersonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PersonListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: PersonListViewHolder, position: Int) {
        val person: UserData = personList[position]
        holder.bind(person)

    }

    override fun getItemCount(): Int = personList.size

    fun updateList(list : List<UserData>) {
        personList = list
        notifyDataSetChanged()
    }
}