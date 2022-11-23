package ua.POE.Task_abon.presentation.ui.userinfo.listener

import android.location.LocationListener

interface MyLocationListener: LocationListener {

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

}