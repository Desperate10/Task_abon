package ua.POE.Task_abon.presentation.userinfo

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import ua.POE.Task_abon.R
import ua.POE.Task_abon.utils.getEmojiByUnicode
import ua.POE.Task_abon.utils.getIcons
import ua.POE.Task_abon.utils.getRawTextFile

class IconsDialogFragment : DialogFragment() {

    private val icons = requireContext().getIcons()

    private val neededIcons: String?
        get() = requireArguments().getString(ICONS)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setTitle("Повідомлення")
            .setMessage(getMessage())
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

    }

    private fun getMessage(): String {
        val currentIcons = neededIcons?.substringAfter(" ") ?: ""

        return icons
            .filter { currentIcons.contains(getEmojiByUnicode(it.emoji!!)) }
            .joinToString("\n") { "${getEmojiByUnicode(it.emoji)} ${it.hint}" }
    }

    companion object {

        private const val TAG = "IconsDialogFragment"
        private const val ICONS = "Icons"

        fun show(fragmentManager: FragmentManager, icons: String) {
            val dialogFragment = IconsDialogFragment()
            dialogFragment.arguments = bundleOf(ICONS to icons)
            dialogFragment.show(fragmentManager, TAG)
        }
    }
}