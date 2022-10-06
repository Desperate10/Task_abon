package ua.POE.Task_abon.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RawRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.POE.Task_abon.domain.model.Icons
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset


fun <T : Any> Fragment.autoCleared(initializer: (() -> T)? = null): AutoClearedValue<T> {
    return AutoClearedValue(this, initializer)
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun View.snackbar(message: String) {
    Snackbar.make(
        this,
        message,
        Snackbar.LENGTH_LONG
    ).also { snackbar ->
        snackbar.setAction("Ok") {
            snackbar.dismiss()
        }
    }.show()
}

fun Resources.getRawTextFile(@RawRes id: Int): ArrayList<Icons> {
    val iconsList = ArrayList<Icons>()
    val inputStream: InputStream = openRawResource(id)

    val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
    reader.readLines().forEach {

        //get a string array of all items in this line
        val items = it.split(";")
        val icon = Icons()
        icon.id = items[0]
        icon.priority = items[1]
        icon.hint = items[2]
        icon.emoji = items[3]
        iconsList.add(icon)
        //do what you want with each item
    }

    return iconsList
}

fun getNeededEmojis(iconsList: ArrayList<Icons>, neededIcons: String): String {
    val mods = neededIcons.split("/", "\\")
    return iconsList.filter { it.id in mods }
        .joinToString { getEmojiByUnicode(it.emoji) }
}

fun getEmojiByUnicode(reactionCode: String?): String {
    val code = reactionCode?.substring(2)?.toInt(16)
    code?.let {
        return String(Character.toChars(code))
    }
    return ""
}

fun ContentResolver.getFileName(fileUri: Uri): String {
    var name = ""
    val returnCursor = this.query(fileUri, null, null, null, null)
    if (returnCursor != null) {
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        name = returnCursor.getString(nameIndex)
        returnCursor.close()
    }
    return name
}

suspend fun <T> saveReadFile(
    readFile: suspend () -> T
): Resource<T> {
    return withContext(Dispatchers.IO) {
        try {
            Resource.Success(readFile.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is FileNotFoundException ->
                    Resource.Error("Файл не найден")
                else -> Resource.Error("Ошибка чтения файла")
            }
        }
    }
}