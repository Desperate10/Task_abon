package ua.POE.Task_abon.presentation.ui.task.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import ua.POE.Task_abon.R
import ua.POE.Task_abon.domain.model.TaskInfo

class TaskClickMenuFragmentDialog : DialogFragment() {

    private var task: String? = null

    override fun onSaveInstanceState(outState: Bundle) {
        task?.let { outState.putString(TASK, it) }
        super.onSaveInstanceState(outState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        task = if (arguments == null) {
            savedInstanceState?.getString(TASK)
        } else {
            requireArguments().getString(TASK)
        }

        val options = arrayOf<CharSequence>(
            getString(R.string.upload_task),
            getString(R.string.clear_field_btn),
            getString(R.string.delete_task),
            getString(R.string.cancel)
        )

        return AlertDialog.Builder(requireContext())
            .setTitle("Виберіть дію:")
            .setItems(options) { _: DialogInterface, item: Int ->
                    parentFragmentManager.setFragmentResult(
                        REQUEST_KEY,
                        bundleOf(TASK to task,
                            KEY_BUTTON to options[item].toString())
                    )
            }
            .setCancelable(true)
            .create()
    }

    companion object {

        private const val TAG = "TaskClickMenuFragmentDialog"
        private const val TASK = "task"
        const val REQUEST_KEY = "$TAG:clearOrNot"
        const val KEY_BUTTON = "button"

        fun show(fragmentManager: FragmentManager, taskInfo: TaskInfo) {
            val dialogFragment = TaskClickMenuFragmentDialog()
            val task = Gson().toJson(taskInfo)
            dialogFragment.arguments = bundleOf(TASK to task)
            dialogFragment.show(fragmentManager, TAG)
        }

        fun setupListeners(
            manager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            listener: (String,String) -> Unit
        ) {
            manager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner) { _, result ->
                listener.invoke(
                    result.getString(TASK, ""),
                    result.getString(KEY_BUTTON, "")
                )
            }
        }
    }
}