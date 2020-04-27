package com.bhoodz.carapp.ui.dialog

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.bhoodz.carapp.R

class LoadingDialog(private val activity: Activity) {

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

