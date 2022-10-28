package ua.POE.Task_abon.presentation.userinfo

import android.view.View
import android.widget.AdapterView

interface ItemSelectedListener: AdapterView.OnItemSelectedListener {

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}