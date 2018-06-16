@file:Suppress("DEPRECATION", "unused")

package cc.aoeiuv020.panovel.util

import android.app.Activity
import android.app.Dialog
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.BaseBundle
import android.os.Build
import android.os.Bundle
import android.support.annotation.WorkerThread
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.main.MainActivity
import cc.aoeiuv020.panovel.report.Reporter
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.dialog_editor.view.*
import org.jetbrains.anko.*
import java.util.concurrent.TimeUnit


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
}.safelyShow()


fun Context.alertColorPicker(initial: Int, callback: (color: Int) -> Unit) = ColorPickerDialogBuilder.with(this)
        .initialColor(initial)
        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
        .setOnColorChangedListener(callback)
        .setPositiveButton(android.R.string.yes) { _, color, _ -> callback(color) }
        // 因为取消前可能已经选了颜色，所以要设置一次初始的颜色，
        .setNegativeButton(android.R.string.cancel) { _, _ -> callback(initial) }
        .build().apply {
            // 去除对话框的灰背景，
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }.safelyShow()

fun Context.notify(id: Int, text: String? = null, title: String? = null, icon: Int = R.mipmap.ic_launcher_foreground, time: Long? = null, bigText: String? = null) {
    val intent = intentFor<MainActivity>()
    val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
    val nb = NotificationCompat.Builder(this)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
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
    val manager = NotificationManagerCompat.from(this)
    manager.notify(id, nb.build())
}

fun Context.cancelNotify(id: Int) {
    val manager = NotificationManagerCompat.from(this)
    manager.cancel(id)
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

fun Activity.setBrightness(brightness: Int) {
    if (brightness < 0) {
        setBrightnessFollowSystem()
    } else {
        window.attributes = window.attributes.apply {
            screenBrightness = minOf(255, brightness) / 255f
        }
    }
}

fun Activity.setBrightnessFollowSystem() {
    window.attributes = window.attributes.apply {
        screenBrightness = -1f
    }
}

/**
 * 没有图片的小说统一用这个填充图片地址，
 * 展示的时候换成内置的图片，
 */
val noCover: String get() = "https://www.snwx8.com/modules/article/images/nocover.jpg"

/**
 * 不希望展示对话框失败导致崩溃，
 */
fun Dialog.safelyShow() {
    try {
        show()
    } catch (e: Exception) {
        val message = "展示对话框失败，"
        Reporter.post(message, e)
    }
}

fun AlertBuilder<*>.safelyShow(): DialogInterface? = try {
    show()
} catch (e: Exception) {
    val message = "展示对话框失败，"
    Reporter.post(message, e)
    null
}

/**
 * 在异步线程调用，在ui线程弹对话框并等待用户输入，
 */
@WorkerThread
fun uiInput(ctx: Context,
            title: Int,
            default: String,
        // 默认就等一分钟，
            timeout: Long = TimeUnit.MINUTES.toMillis(1)
): String? {
    // TODO: 考虑试试kotlin的协程，
    val thread = Thread.currentThread()
    var result: String? = null
    var sleeping = false
    synchronized(thread) {
        var dialog: DialogInterface? = null
        ctx.runOnUiThread {
            dialog = ctx.alert {
                titleResource = title
                val layout = View.inflate(ctx, R.layout.dialog_editor, null)
                customView = layout
                val etName = layout.editText
                etName.setText(default)
                yesButton {
                    result = etName.text.toString()
                    if (sleeping) {
                        // 如果已经超时，中断就不知道会影响到什么了，
                        thread.interrupt()
                    }
                }
                onCancelled {
                    if (sleeping) {
                        thread.interrupt()
                    }
                }
            }.safelyShow()
        }
        try {
            sleeping = true
            Thread.sleep(timeout)
            sleeping = false
            // dialog可以异步dismiss,
            dialog?.dismiss()
        } catch (_: InterruptedException) {
        }
    }
    return result
}
