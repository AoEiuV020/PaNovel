package cc.aoeiuv020.reader

import android.view.View

/**
 *
 * Created by AoEiuV020 on 2017.12.01-15:23:42.
 */
fun View.setHeight(height: Int) {
    layoutParams = layoutParams.also { it.height = height }
}

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}
