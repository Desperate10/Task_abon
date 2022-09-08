package ua.POE.Task_abon.utils

import android.net.Uri

class Image {
    private var imgURI: Uri? = null

    fun getURI(): Uri? {
        return imgURI
    }

    fun setURI(imgURI: Uri?) {
        this.imgURI = imgURI
    }
}