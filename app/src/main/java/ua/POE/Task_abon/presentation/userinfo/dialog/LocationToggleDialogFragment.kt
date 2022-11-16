package ua.POE.Task_abon.presentation.userinfo.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import ua.POE.Task_abon.R

class LocationToggleDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setMessage("Ввімкнути GPS?").setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { _: DialogInterface?, _: Int ->
                startActivity(
                    Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    )
                )
            }
            .setNegativeButton(getString(R.string.no)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .create()

    }

    companion object {
        const val TAG = "LocationToggleDialogFragment"

        fun show(manager: FragmentManager) {
            val dialogFragment = SaveCoordinatesDialogFragment()
            dialogFragment.show(manager, TAG)
        }
    }
}