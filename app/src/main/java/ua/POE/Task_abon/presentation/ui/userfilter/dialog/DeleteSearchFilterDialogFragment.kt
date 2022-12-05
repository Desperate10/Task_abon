package ua.POE.Task_abon.presentation.ui.userfilter.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner

class DeleteSearchFilterDialogFragment: DialogFragment() {

    private var position: Int? = null

    override fun onSaveInstanceState(outState: Bundle) {
        position?.let { outState.putInt(POSITION, it) }
        super.onSaveInstanceState(outState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        position = if (arguments == null) {
            savedInstanceState?.getInt(POSITION)
        } else {
            requireArguments().getInt(POSITION)
        }

        val options = arrayOf<CharSequence>("Видалити фільтр", "Відміна")
        return AlertDialog.Builder(requireContext())
        .setTitle("Виберіть дію:")
        .setItems(options) { dialog: DialogInterface, item: Int ->
            when {
                options[item] == "Видалити фільтр" -> {
                    parentFragmentManager.setFragmentResult(
                        REQUEST_KEY,
                        bundleOf(POSITION to position)
                    )
                }
                else -> {
                    dialog.dismiss()
                }
            }
        }
            .setCancelable(true)
            .create()

    }

    companion object {
        private const val TAG = "DeleteSearchFilterDialogFragment"
        const val REQUEST_KEY = "$TAG:delete"
        private const val POSITION = "position"

        fun show(fragmentManager: FragmentManager, position: Int) {
            val fragment = DeleteSearchFilterDialogFragment()
            fragment.arguments = bundleOf(POSITION to position)
            fragment.show(fragmentManager, TAG)
        }

        fun setupListener(
            manager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            listener: (Int) -> Unit
        ) {
            manager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner) { _, result ->
                listener.invoke(
                    result.getInt(POSITION)
                )
            }
        }
    }
}