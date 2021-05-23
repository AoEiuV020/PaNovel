package cc.aoeiuv020.panovel.ad

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import java.util.concurrent.TimeUnit

/**
 * Created by AoEiuV020 on 2021.05.23-17:01:37.
 */
object SplashAdWrapper : Application.ActivityLifecycleCallbacks, ComponentCallbacks2, AnkoLogger {
    private const val DEBUG = false
    var lastAdShowTime: Long = 0
    private var isBackground: Boolean = false

    fun init(app: Application) {
        // 只注册不取消，毕竟跟着整个app生命周期，
        app.registerActivityLifecycleCallbacks(this)
        app.registerComponentCallbacks(this)
    }

    fun startSplashAd(ctx: Context) {
        if (ctx is SplashActivity) {
            // 已经是打开splash的情况就不重复打开了，
            return
        }
        if (!checkCd(AdConstants.CD_SPLASH_BACKGROUND)) {
            // 冷却30分钟后，后台恢复时打开启动页广告，
            ctx.startActivity<SplashActivity>()
        }
    }

    /**
     * @return 如果在冷却时间内，返回true，
     */
    fun checkCd(minute: Int): Boolean {
        if (DEBUG) {
            return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastAdShowTime) < minute
        } else {
            return TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastAdShowTime) < minute
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {
        if (isBackground) {
            info { "app进入前台" }
            isBackground = false
            startSplashAd(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    override fun onConfigurationChanged(newConfig: Configuration) {

    }

    override fun onLowMemory() {

    }

    override fun onTrimMemory(level: Int) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            info { "app进入后台" }
            isBackground = true
        }
    }

}