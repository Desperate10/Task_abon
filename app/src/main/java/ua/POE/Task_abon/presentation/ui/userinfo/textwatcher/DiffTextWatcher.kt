package ua.POE.Task_abon.presentation.ui.userinfo.textwatcher

import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged

object DiffTextWatcher {

    fun registerWatcher(
        newMeter: EditText,
        difference: TextView,
        oldMeter: TextView
    ): TextWatcher? {
        return try {
            newMeter.doAfterTextChanged {
                if (!it.isNullOrEmpty() && oldMeter.text.toString().toIntOrNull() != null) {
                    difference.text = (
                            it.toString().toInt() - oldMeter.text.toString()
                                .toInt()).toString()
                } else {
                    difference.text = ""
                }
            }
            oldMeter.doAfterTextChanged {
                if (it.toString().toIntOrNull() != null && newMeter.text.toString().isNotEmpty()) {
                    difference.text = (
                            newMeter.text.toString().toInt() - it.toString()
                                .toInt()).toString()
                } else {
                    difference.text = ""
                }
            }
        } catch (e: NumberFormatException) {
            return null
        }
    }
}