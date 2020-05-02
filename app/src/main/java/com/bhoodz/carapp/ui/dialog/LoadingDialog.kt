package com.bhoodz.carapp.ui.dialog

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.bhoodz.carapp.R

//private const val TAG = "LoadingDialog"

class LoadingDialog(activity: Activity) {

    private var dialog: AlertDialog? = null

    init {
        val alertDialog = AlertDialog.Builder(activity)
        alertDialog.setView(R.layout.loading_dialog)
        alertDialog.setCancelable(false)
        dialog = alertDialog.create()
    }

    fun show() {
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}

