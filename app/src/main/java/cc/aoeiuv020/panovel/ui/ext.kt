@file:Suppress("DEPRECATION", "unused")

package cc.aoeiuv020.panovel.ui

import android.app.ProgressDialog
import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.View
import cc.aoeiuv020.panovel.R

/**
 *
 * Created by AoEiuV020 on 2017.10.02-21:50:34.
 */
fun Context.loading(dialog: ProgressDialog, id: Int) = loading(dialog, getString(R.string.loading, getString(id)))

fun Context.loading(dialog: ProgressDialog, str: String) = dialog.apply {
    setMessage(str)
    show()
}

fun Context.alertError(dialog: AlertDialog, str: String, e: Throwable) = alert(dialog, str + "\n" + e.message)
fun Context.alert(dialog: AlertDialog, messageId: Int) = alert(dialog, getString(messageId))
fun Context.alert(dialog: AlertDialog, messageId: Int, titleId: Int) = alert(dialog, getString(messageId), getString(titleId))
fun Context.alert(dialog: AlertDialog, message: String, title: String? = null) = dialog.apply {
    dialog.setMessage(message)
    title?.let {
        dialog.setTitle(title)
    }
    show()
}

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

