@file:Suppress("DEPRECATION", "unused")

package cc.aoeiuv020.panovel.util

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.BaseBundle
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import cc.aoeiuv020.panovel.R
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
fun Dialog.safelyShow(): DialogInterface {
    try {
        show()
    } catch (e: Exception) {
        val message = "展示对话框失败，"
        Reporter.post(message, e)
    }
    return this
}

fun AlertBuilder<*>.safelyShow(): DialogInterface? = try {
    show()
} catch (e: Exception) {
    val message = "展示对话框失败，"
    Reporter.post(message, e)
    null
}

/**
 * 异步线程弹单选框并等待用户选择，
 *
 * @return 返回用户选择的元素序号，取消就返回null,
 */
@WorkerThread
fun Context.uiSelect(
        // 要选择的是什么，展示在对话框标题，
        name: String,
        items: Array<String>,
        default: Int,
        // 默认就等一分钟，
        timeout: Long = TimeUnit.MINUTES.toMillis(1)
): Int? {
    val thread = Thread.currentThread()
    var result: Int? = null
    var sleeping = false
    synchronized(thread) {
        var dialog: DialogInterface? = null
        runOnUiThread {
            dialog = AlertDialog.Builder(ctx).apply {
                setTitle(ctx.getString(R.string.select_placeholder, name))
                setSingleChoiceItems(items, default) { dialog, which ->
                    dialog.dismiss()
                    result = which
                    if (sleeping) {
                        // 如果已经超时，中断就不知道会影响到什么了，
                        thread.interrupt()
                    }
                }
                setOnCancelListener {
                    if (sleeping) {
                        thread.interrupt()
                    }
                }
            }.create().safelyShow()
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

/**
 * 在异步线程调用，在ui线程弹对话框并等待用户输入，
 */
@WorkerThread
fun Context.uiInput(
        // 要输入的是什么，展示在对话框标题，
        name: String,
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
        runOnUiThread {
            dialog = alert {
                title = ctx.getString(R.string.input_placeholder, name)
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
