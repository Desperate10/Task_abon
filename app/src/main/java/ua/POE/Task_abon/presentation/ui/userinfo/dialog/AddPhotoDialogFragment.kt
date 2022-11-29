package ua.POE.Task_abon.presentation.ui.userinfo.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import ua.POE.Task_abon.R

class AddPhotoDialogFragment : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val options = arrayOf<CharSequence>(
            getString(R.string.replace_photo),
            getString(R.string.delete_photo),
            getString(R.string.cancel)
        )


        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.choose_action)
            .setItems(options) { dialog: DialogInterface, item: Int ->
                parentFragmentManager.setFragmentResult(
                    REQUEST_KEY,
                    bundleOf(
                        KEY_BUTTON to options[item].toString()
                    )
                )
            }
            .setCancelable(true)
            .create()
    }

    companion object {
        private const val TAG = "AddPhotoDialogFragment"
        private const val URI = "photoUri"
        private const val REQUEST_KEY = "$TAG:addPhoto"
        private const val KEY_BUTTON = "button"

        fun show(fragmentManager: FragmentManager) {
            val fragment = AddPhotoDialogFragment()
           // fragment.arguments = bundleOf(URI to uri)
            fragment.show(fragmentManager, TAG)
        }

        fun setupListeners(
            manager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            listener: (String) -> Unit
        ) {
            manager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner) { _, result->
                listener.invoke(
                    result.getString(KEY_BUTTON, "")
                )
            }
        }
    }
}