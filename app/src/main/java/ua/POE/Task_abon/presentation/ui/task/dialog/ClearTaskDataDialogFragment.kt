package ua.POE.Task_abon.presentation.ui.task.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import ua.POE.Task_abon.R

class ClearTaskDataDialogFragment : DialogFragment() {

    private var taskId:Int? = null

    override fun onSaveInstanceState(outState: Bundle) {
        taskId?.let { outState.putInt(TASK_ID, it) }
        super.onSaveInstanceState(outState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        taskId = if (arguments == null) {
            savedInstanceState?.getInt(TASK_ID)
        } else {
            requireArguments().getInt(TASK_ID)
        }

        val listener = DialogInterface.OnClickListener() { _, _ ->
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                bundleOf(TASK_ID to taskId)
            )
        }

        return AlertDialog.Builder(requireContext())
            .setMessage("Ви впевнені, що хочете видалити збережені дані?")
            .setPositiveButton(getString(R.string.yes), listener)
            .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .create()
    }

    companion object {
        private const val TAG = "ClearTaskDataDialogFragment"
        const val REQUEST_KEY = "$TAG:clear"
        const val TASK_ID = "task_id"

        fun show(fragmentManager: FragmentManager, taskId: Int) {
            val dialog = ClearTaskDataDialogFragment()
            dialog.arguments = bundleOf(TASK_ID to taskId)
            dialog.show(fragmentManager, TAG)
        }

        fun setupListeners(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            listener: (Int) -> Unit
        ) {
            fragmentManager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner) { _, result ->
                listener.invoke(
                    result.getInt(TASK_ID)
                )

            }
        }
    }
}