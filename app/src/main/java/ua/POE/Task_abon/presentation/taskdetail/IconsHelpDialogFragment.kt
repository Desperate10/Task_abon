package ua.POE.Task_abon.presentation.taskdetail

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import ua.POE.Task_abon.R
import ua.POE.Task_abon.domain.model.Icons
import ua.POE.Task_abon.utils.getEmojiByUnicode
import javax.inject.Inject

class IconsHelpDialogFragment : DialogFragment() {

    @Inject
    lateinit var icons: List<Icons>

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