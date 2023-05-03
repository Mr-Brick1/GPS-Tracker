package com.mr_brick.gps_tracker.utils

import android.app.AlertDialog
import android.content.Context
import com.mr_brick.gps_tracker.R

object DialogManager {
    fun showLocEnableDialog(context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle(R.string.location_disabled)
        dialog.setMessage(context.getString(R.string.location_dialog_message))
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.yes)) { _, _ ->
            listener.onClick()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.no)) { _, _ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    fun

    interface Listener {
        fun onClick()
    }

}