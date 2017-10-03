package cc.aoeiuv020.panovel.ui.base

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.ui.hide
import cc.aoeiuv020.panovel.ui.show
import kotlinx.android.synthetic.main.activity_novel_text.*
import org.jetbrains.anko.AnkoLogger

/**
 * 全屏Activity，绝大部分代码是自动生成的，
 * 分离出来仅供activity_novel_text使用，
 * Created by AoEiuV020 on 2017.09.15-17:38.
 */
@Suppress("MemberVisibilityCanPrivate", "unused")
abstract class NovelTextBaseFullScreenActivity : AppCompatActivity(), AnkoLogger {
    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        viewPager.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        app_bar.show()
        fullscreen_content_controls.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        @Suppress("ConstantConditionIf")
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.itemId.let {
            when (it) {
                android.R.id.home -> onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_novel_text)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        mVisible = true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        delayedHide(100)
    }

    fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    protected fun hide() {
        app_bar.hide()
        fullscreen_content_controls.visibility = View.GONE
        mVisible = false
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    protected fun show() {
        viewPager.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        private val AUTO_HIDE = true
        private val AUTO_HIDE_DELAY_MILLIS = 3000
        private val UI_ANIMATION_DELAY = 300
    }
}

class NovelTextFullScreenRootFrameLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs), AnkoLogger {
    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    override fun dispatchApplyWindowInsets(insets: WindowInsets): WindowInsets {
        findViewById<View>(R.id.navBarBg)?.apply {
            layoutParams = layoutParams.apply { height = insets.systemWindowInsetBottom }
        }
        return super.dispatchApplyWindowInsets(insets)
    }
}