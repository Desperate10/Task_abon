package ua.POE.Task_abon.presentation.ui.taskdetail.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import ua.POE.Task_abon.R
import ua.POE.Task_abon.presentation.model.Icons
import ua.POE.Task_abon.utils.getEmojiByUnicode
import ua.POE.Task_abon.utils.getIcons

class IconsHelpDialogFragment : DialogFragment() {

    private lateinit var icons: List<Icons>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        icons = context.getIcons()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setTitle("Умовні позначки")
            .setMessage(getMessage())
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    private fun getMessage(): String {
        return icons
            .joinToString("\n") { "${getEmojiByUnicode(it.emoji)} ${it.hint}" }
    }

    companion object {

        private const val TAG = "IconsHelpDialogFragment"

        fun show(fragmentManager: FragmentManager) {
            val dialogFragment = IconsHelpDialogFragment()
            dialogFragment.show(fragmentManager, TAG)
        }
    }
}