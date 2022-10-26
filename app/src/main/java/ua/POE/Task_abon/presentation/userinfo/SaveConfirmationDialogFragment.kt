package ua.POE.Task_abon.presentation.userinfo

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import ua.POE.Task_abon.R

class SaveConfirmationDialogFragment : DialogFragment() {

    private val isForward: Boolean
        get() = requireArguments().getBoolean(ARG_DIRECTION)

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(ARG_DIRECTION, isForward)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = DialogInterface.OnClickListener { which, _ ->
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                bundleOf(KEY_DIRECTION_RESPONSE to isForward, KEY_BUTTON to which)
            )
        }
        return AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setMessage("Зберегти зміни?")
            .setPositiveButton(getString(R.string.yes), listener)
            .setNegativeButton(getString(R.string.no), listener)
            .create()
    }

    companion object {
        const val TAG = "SaveConfirmationDialogFragment"
        const val REQUEST_KEY = "$TAG:saveOrNot"
        const val KEY_BUTTON = "BUTTON"
        const val KEY_DIRECTION_RESPONSE = "RESPONSE"
        const val ARG_DIRECTION = "ARG_DIRECTION"

        fun show(manager: FragmentManager, isForward: Boolean) {
            val dialogFragment = SaveConfirmationDialogFragment()
            dialogFragment.arguments = bundleOf(ARG_DIRECTION to isForward)
            dialogFragment.show(manager, TAG)
        }

        fun setupListeners(
            manager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            listener: (Boolean, Int) -> Unit
        ) {
            manager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner) { _, result ->
                listener.invoke(result.getBoolean(KEY_DIRECTION_RESPONSE), result.getInt(KEY_BUTTON))
            }
        }
    }
}