@file:Suppress("DEPRECATION", "unused")

package cc.aoeiuv020.panovel.util

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.BaseBundle
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.main.MainActivity
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.dialog_editor.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.yesButton


/**
 *
 * Created by AoEiuV020 on 2017.10.02-21:50:34.
 */

/**
 * 这个自带"加载中.."
 */
fun Context.loading(dialog: ProgressDialog, id: Int) = loading(dialog, getString(R.string.loading, getString(id)))

fun Context.loading(dialog: ProgressDialog, str: String) = dialog.apply {
    setTitle(null)
    setMessage(str)
    show()
}

fun Context.alertError(dialog: AlertDialog, str: String, e: Throwable) = alert(dialog, str + "\n" + e.message)
fun Context.alert(dialog: AlertDialog, messageId: Int) = alert(dialog, getString(messageId))
fun Context.alert(dialog: AlertDialog, messageId: Int, titleId: Int) = alert(dialog, getString(messageId), getString(titleId))
fun Context.alert(dialog: AlertDialog, message: String, title: String? = null) = dialog.apply {
    setMessage(message)
    title?.let {
        setTitle(title)
    }
    show()
}

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.setSize(size: Int) {
    layoutParams = layoutParams.also {
        it.height = size
        it.width = size
    }
}

fun View.setHeight(height: Int) {
    layoutParams = layoutParams.also { it.height = height }
}

fun Context.changeColor(initial: Int, callback: (color: Int) -> Unit) = alert {
    titleResource = R.string.colorARGB
    val layout = View.inflate(this@changeColor, R.layout.dialog_editor, null)
    customView = layout
    val etColor = layout.editText.apply {
        setText(java.lang.Integer.toHexString(initial).toUpperCase())
    }
    neutralPressed(R.string.picker) {
        alertColorPicker(initial, callback)
    }
    yesButton {
        try {
            val iColor = etColor.text.toString().toLong(16).toInt()
            callback(iColor)
        } catch (e: NumberFormatException) {
        }
    }
    cancelButton { }
}.show()


fun Context.alertColorPicker(initial: Int, callback: (color: Int) -> Unit) = ColorPickerDialogBuilder.with(this)
        .initialColor(initial)
        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
        .setOnColorChangedListener(callback)
        .setPositiveButton(android.R.string.yes) { _, color, _ -> callback(color) }
        // 因为取消前可能已经选了颜色，所以要设置一次初始的颜色，
        .setNegativeButton(android.R.string.cancel) { _, _ -> callback(initial) }
        .build().apply {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }.show()

fun Context.notify(id: Int, text: String? = null, bigText: String? = null, title: String? = null, icon: Int = R.mipmap.ic_launcher_foreground, time: Long? = null) {
    val intent = intentFor<MainActivity>()
    val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
    val nb = NotificationCompat.Builder(this)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
    bigText?.let {
        nb.setStyle(NotificationCompat.BigTextStyle().bigText(it))
    }
    time?.let {
        nb.setWhen(it)
    }
    nb.apply {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            setLargeIcon(getBitmapFromVectorDrawable(icon))
            setSmallIcon(R.mipmap.ic_launcher_round)
        } else {
            setSmallIcon(icon)
        }
    }
    val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
    manager.notify(id, nb.build())
}

/**
 * https://stackoverflow.com/a/38244327/5615186
 */
fun Context.getBitmapFromVectorDrawable(drawableId: Int): Bitmap {
    var drawable = ContextCompat.getDrawable(this, drawableId)!!
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        drawable = DrawableCompat.wrap(drawable).mutate()
    }

    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}

fun EditText.showKeyboard() {
    if (hasFocus()) {
        clearFocus()
    }
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, 0)
}

@Suppress("UNCHECKED_CAST")
fun Bundle.toMap(): Map<String, Any?> = BaseBundle::class.java.getDeclaredField("mMap").apply { isAccessible = true }
        .get(this) as Map<String, *>

// 从保存的状态或者传入的intent中拿String,
fun Activity.getStringExtra(key: String, savedInstanceState: Bundle? = null): String? = savedInstanceState?.run { getString(key) }
        ?: intent.getStringExtra(key)
