package cz.cvut.fel.thethronelocator.utils

import android.view.View

import com.google.android.material.snackbar.Snackbar


object SnackBarUtils {
    fun showSnackBarWithCloseButton(view: View, message: String) {
        val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        snackBar.setAction("Close") { snackBar.dismiss() }
        snackBar.show()
    }
}

//example of callign this

//SnackBarUtils.showSnackBarWithCloseButton(findViewById<View>(R.id.toiletList),"New toilet successfully added")
