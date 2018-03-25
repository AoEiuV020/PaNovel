package cc.aoeiuv020.panovel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings.Secure
import android.support.v7.app.AppCompatDelegate
import cc.aoeiuv020.panovel.api.paNovel
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.util.asyncExecutor
import cc.aoeiuv020.panovel.util.ignoreException
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tencent.bugly.crashreport.CrashReport
import io.reactivex.internal.functions.Functions
import io.reactivex.plugins.RxJavaPlugins
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import java.io.File


/**
 *
 * Created by AoEiuV020 on 2017.10.03-17:04:22.
 */
@Suppress("MemberVisibilityCanPrivate")
class App : Application(), AnkoLogger {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var ctx: Context
        val gsonBuilder: GsonBuilder = GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .paNovel()
        val gson: Gson = gsonBuilder.create()

        lateinit var adRequest: AdRequest
    }

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext

        // 有相关问题不明原因，以防万一，加个判断sd上的私有目录是否可写，
        checkBaseFile(ctx.getExternalFilesDir(null))

        // 低版本api(<=20)默认不能用矢量图的selector, 要这样设置，
        // 还有ContextCompat.getDrawable也不行，
        // it's not a BUG, it's a FEATURE,
        // https://issuetracker.google.com/issues/37100284
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        // 无视RxJava抛的异常，也就是不被捕获调用onError的异常，
        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer())

        MobileAds.initialize(this, "ca-app-pub-3036112914192534~4631187497")
        adRequest = AdRequest.Builder()
                .also { builder ->
                    // 添加广告的测试设备，
                    try {
                        assets.open("admob_test_device_list").reader().readLines().filter(String::isNotBlank).forEach {
                            debug { "add test device: $it" }
                            builder.addTestDevice(it)
                        }
                    } catch (_: Exception) {
                        // 什么都不做，
                    }
                }.build()

        // 第三个参数为SDK调试模式开关，
        // 模拟器打开，
        CrashReport.initCrashReport(ctx, "be0d684a75", adRequest.isTestDevice(ctx))
        // 貌似设置了开发设备就不上报了，
        CrashReport.setIsDevelopmentDevice(ctx, !Settings.reportCrash)

        val androidId = Secure.getString(ctx.contentResolver, Secure.ANDROID_ID)
        CrashReport.setUserId(androidId)
        // 异步设置bugly的用户ID，获取的是google的广告ID,不能在主线程，
        asyncExecutor.execute {
            try {
                val adId = AdvertisingIdClient.getAdvertisingIdInfo(ctx).id
                CrashReport.setUserId(adId)
            } catch (_: Exception) {
            }
            info {
                "Bugly user id -> ${CrashReport.getUserId()}"
            }
        }
    }

    private fun checkBaseFile(file: File) {
        file.resolve("test").let {
            it.exists() || ignoreException { it.writeText("true") }
        }.takeIf { it }?.let {
            Settings.baseFile = file
        }
    }
}
