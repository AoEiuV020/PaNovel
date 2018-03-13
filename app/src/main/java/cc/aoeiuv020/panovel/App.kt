package cc.aoeiuv020.panovel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.support.v7.app.AppCompatDelegate
import cc.aoeiuv020.panovel.api.paNovel
import cc.aoeiuv020.panovel.local.Settings
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tencent.bugly.crashreport.CrashReport
import io.reactivex.internal.functions.Functions
import io.reactivex.plugins.RxJavaPlugins
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug


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

    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext

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
    }
}
