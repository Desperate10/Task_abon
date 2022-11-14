package ua.POE.Task_abon.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Toast
import ua.POE.Task_abon.R
import java.util.*

class ImageAdapter(val context: Context, val data: ArrayList<Image>, private val uri: ArrayList<String>) : BaseAdapter() {

    override fun getCount(): Int = data.size

    override fun getItem(position: Int) = data[position]

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = parent?.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.image_grid_layout, null)
        val imageView = view.findViewById<ImageView>(R.id.grid_item_image)
        val deleteImage = view.findViewById<ImageView>(R.id.iv_delete)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageURI(data[position].getURI())

        if(position == 0) {
            deleteImage.visibility = View.GONE
        } else {
            deleteImage.visibility = View.VISIBLE
        }

        deleteImage.setOnClickListener {
            try {
                context.contentResolver.delete(data[position].getURI()!!, null, null)
            } catch (e: SecurityException) {
                Toast.makeText(context, "Фото зроблені не з допомогую додатку требу видаляти вручну", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Помилка видалення фото", Toast.LENGTH_SHORT).show()
            }
            data.removeAt(position)
            uri.removeAt(position-1)
            notifyDataSetChanged()
        }
        return view
    }
}