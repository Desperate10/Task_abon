package ua.POE.Task_abon.presentation.task.dialog

import android.app.Dialog
import android.app.TaskInfo
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import ua.POE.Task_abon.R

class ClearTaskDataDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val listener = DialogInterface.OnClickListener { _, which ->
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                bundleOf(KEY_BUTTON to which)
            )
        }

        return AlertDialog.Builder(requireContext())
            .setMessage("Ви впевнені, що хочете видалити збережені дані?")
            .setPositiveButton(getString(R.string.yes), listener)
            .setNegativeButton(getString(R.string.no), listener)
            .create()
    }

    companion object {

        private const val TAG = "ClearTaskDataDialog"
        const val REQUEST_KEY = "$TAG:clearOrNot"
        const val KEY_BUTTON = "button"

        fun show(fragmentManager: FragmentManager, taskInfo: TaskInfo) {
            val dialogFragment = ClearTaskDataDialog()
            dialogFragment.show(fragmentManager, TAG)
        }

        fun setupListeners(
            manager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            listener: (Int) -> Unit
        ) {
            manager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner) { _, result ->
                listener.invoke(
                    result.getInt(KEY_BUTTON)
                )
            }
        }
    }
}