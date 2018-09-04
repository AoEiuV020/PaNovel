package cc.aoeiuv020.panovel.report

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import cc.aoeiuv020.panovel.BuildConfig
import cc.aoeiuv020.panovel.api.NoInternetException
import cc.aoeiuv020.panovel.settings.OtherSettings
import com.tencent.bugly.crashreport.CrashReport
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.net.UnknownHostException

/**
 * 封装异常上报，
 * 当前使用，腾讯的bugly,
 *
 * Created by AoEiuV020 on 2018.05.17-18:04:30.
 */
object Reporter : AnkoLogger {
    @SuppressLint("HardwareIds")
    fun init(ctx: Context) {
        // 第三个参数为SDK调试模式开关，
        // 打开会导致开发机上报异常，
        CrashReport.initCrashReport(ctx, "be0d684a75", BuildConfig.DEBUG && Log.isLoggable("Bugly", Log.DEBUG))
        // 貌似设置了开发设备就不上报了，
        // 如果设置里关闭上报异常或者是调试模式，就设置为开发者，
        CrashReport.setIsDevelopmentDevice(ctx, !OtherSettings.reportCrash || BuildConfig.DEBUG)

        val androidId = android.provider.Settings.Secure.getString(ctx.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
        CrashReport.setUserId(androidId)
    }

    fun unreachable() {
        post("不可到达，")
    }

    /**
     * requireNotNull,
     */
    fun <T> notNullOrReport(t: T?, value: String): T {
        if (t == null) {
            val message = "<$value>不可空，"
            val e = IllegalArgumentException(message)
            // 可能有重复上报，抛出去的异常可能再被捕获然后上报，但不嫌多，
            post(message, e)
            throw  e
        }
        return t
    }

    fun post(message: String) {
        val e = IllegalStateException(message)
        debug(message, e)
        postException(e)
    }

    fun unreachable(e: Throwable) {
        post("不可到达，", e)
    }

    fun post(message: String, e: Throwable) {
        debug(message, e)
        postException(IllegalStateException(message, e))
    }

    /**
     * 判断该异常是否是不存在网络连接导致的，
     */
    private fun isNoInternetException(e: Throwable): Boolean {
        if (e is NoInternetException
                || e is UnknownHostException) {
            return true
        }
        if (e.message?.contains("No address associated with hostname") == true) {
            // 有的设备报的不是UnknownHostException，原因不明，
            // android_getaddrinfo failed: EAI_NODATA (No address associated with hostname)
            return true
        }
        return false
    }

    private fun postException(e: Throwable) {
        // 开发过程不要上报，
        if (BuildConfig.DEBUG) {
            return
        }
        var cause: Throwable? = e
        while (cause != null) {
            if (isNoInternetException(cause)) {
                // 没有网络连接导致的异常不上报，
                return
            }
            // 以防万一，虽然应该不会出现cause就是本身导致死循环，
            cause = cause.cause.takeIf { it != cause }
        }
        CrashReport.postCatchedException(e)
    }
}