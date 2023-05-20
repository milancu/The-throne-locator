package cz.cvut.fel.thethronelocator.utils

import android.view.View
import android.view.View.OnClickListener

import com.google.android.material.snackbar.Snackbar


object SnackBarUtils {
    fun showSnackBarWithCloseButton(view: View, message: String) {
        val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        snackBar.setAction("Close") { snackBar.dismiss() }
        snackBar.show()
    }

    fun showSnackBarWithAction(
        view: View,
        message: String,
        actionText: String,
        listener: OnClickListener
    ) {
        val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
        snackBar.setAction(actionText, listener)
        snackBar.show()
    }
}

//example of callign this

//SnackBarUtils.showSnackBarWithCloseButton(findViewById<View>(R.id.toiletList),"New toilet successfully added")
