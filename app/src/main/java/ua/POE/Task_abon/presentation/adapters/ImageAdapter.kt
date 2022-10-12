package ua.POE.Task_abon.presentation.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Toast
import ua.POE.Task_abon.R
import ua.POE.Task_abon.domain.model.Image
import java.util.*

class ImageAdapter(
    private val context: Context,
    private val data: ArrayList<Image>,
    private val uri: ArrayList<String>
) : BaseAdapter() {



    override fun getCount(): Int = data.size

    override fun getItem(position: Int) = data[position]

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        if (convertView == null) {
            val inflater =
                parent?.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.image_grid_layout, parent, false)
            val imageView = view.findViewById<ImageView>(R.id.grid_item_image)
            val deleteImage = view.findViewById<ImageView>(R.id.iv_delete)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageURI(data[position].imgURI)

            if (position == 0) {
                deleteImage.visibility = View.GONE
            } else {
                deleteImage.visibility = View.VISIBLE
            }

            addAddButton(context)

            deleteImage.setOnClickListener {
                try {
                    context.contentResolver.delete(data[position].imgURI!!, null, null)
                } catch (e: SecurityException) {
                    Toast.makeText(
                        context,
                        "Фото зроблені не з допомогую додатку требу видаляти вручну",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Помилка видалення фото", Toast.LENGTH_SHORT).show()
                }
                data.removeAt(position)
                uri.removeAt(position - 1)
                notifyDataSetChanged()
            }
            return view
        }
        return convertView
    }

    private fun addAddButton(context: Context) {
        val selectedImage =
            Uri.parse("android.resource://" + context.packageName + "/" + R.drawable.ic_add_photo)
        val i = Image(selectedImage)
        data.add(i)
    }

    fun deletePhoto(context: Context) {
        data.clear()
        addAddButton(context)
    }

    fun addSavedPhoto(uri: Uri) {
        data.add(Image(uri))
        notifyDataSetChanged()
    }

    fun addNewPhoto(imageUri: Uri ) {
        val i = Image(imageUri)
        data.add(i)
        uri.add(imageUri.toString())
        notifyDataSetChanged()
    }
}