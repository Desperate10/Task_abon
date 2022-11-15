package ua.POE.Task_abon.presentation.userinfo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import ua.POE.Task_abon.R
import ua.POE.Task_abon.presentation.model.Icons
import ua.POE.Task_abon.utils.getEmojiByUnicode
import ua.POE.Task_abon.utils.getIcons

class IconsDialogFragment : DialogFragment() {

    private lateinit var icons: List<Icons>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        icons = context.getIcons()
    }

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

        return if(currentIcons.isNotEmpty()) {
            icons
                .filter { currentIcons.contains(getEmojiByUnicode(it.emoji!!)) }
                .joinToString("\n") { "${getEmojiByUnicode(it.emoji)} ${it.hint}" }
        } else {
            getString(R.string.no_needed_icons)
        }
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