package ua.POE.Task_abon.presentation.ui.userinfo.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import ua.POE.Task_abon.R

class SaveCoordinatesDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = DialogInterface.OnClickListener { _, which ->
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                bundleOf(KEY_BUTTON to which)
            )
        }
        return AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setMessage("Впевнені, що хочете зберегти без координат?")
            .setPositiveButton(getString(R.string.yes), listener)
            .setNegativeButton(getString(R.string.no), listener)
            .create()
    }

    companion object {
        const val TAG = "SaveCoordinatesDialogFragment"
        const val REQUEST_KEY = "$TAG:saveOrNot"
        const val KEY_BUTTON = "BUTTON"

        fun show(manager: FragmentManager) {
            val dialogFragment = SaveCoordinatesDialogFragment()
            dialogFragment.show(manager, TAG)
        }

        fun setupListeners(
            manager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            listener: (Int) -> Unit
        ) {
            manager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner) { _, result ->
                listener.invoke(result.getInt(KEY_BUTTON))
            }
        }
    }
}