package com.bhoodz.carapp.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment

const val DIALOG_MESSAGE = "message"
const val DIALOG_DATA = "data"

class DeleteDialog : AppCompatDialogFragment() {

    private var dialogEvents: DeleteDialogEvent? = null

    internal interface DeleteDialogEvent {
        fun onDeleteConfirm(data: Parcelable)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        //Activities/Fragments containing this fragment must implement its callbacks
        dialogEvents = try {
            //Is there a parent fragment? If so, that will be what we call back
            parentFragment as DeleteDialogEvent
        } catch (e: TypeCastException) {
            try {
                //No parent fragment, so call back activity instead
                context as DeleteDialogEvent
            } catch (e: ClassCastException) {
                //Activity doesn't implement the interface
                throw ClassCastException("Activity $context must implement AppDialog.DialogEvents interface")
            }
        } catch (e: ClassCastException) {
            //Activity doesn't implement the interface
            throw ClassCastException("Activity $parentFragment must implement AppDialog.DialogEvents interface")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val arguments = arguments
        val messageString: String?
        val data: Parcelable?

        if (arguments != null) {
            messageString = arguments.getString(DIALOG_MESSAGE)
            data = arguments.getParcelable(DIALOG_DATA)

            if (messageString == null || data == null) {
                throw IllegalStateException("DIALOG_DATA and/or DIALOG_MESSAGE not present in the bundle")
            }
        } else {
            throw IllegalStateException("Must pass DIALOG_DATA and DIALOG_MESSAGE in the bundle")
        }

        return builder.setMessage(messageString)
            .setPositiveButton("OK") { _, _ ->
                dialogEvents?.onDeleteConfirm(data)
            }
            .setNegativeButton("Cancel") { _, _ ->
                //Nothing to implement
            }
            .create()
    }

    override fun onDetach() {
        super.onDetach()

        //Reset the active callback interface, because we're no longer attached
        dialogEvents = null
    }
}