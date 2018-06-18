package cc.aoeiuv020.panovel

import android.annotation.SuppressLint
import android.content.Context
import android.support.multidex.MultiDexApplication
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import cc.aoeiuv020.base.jar.gsonJsonPathInit
import cc.aoeiuv020.base.jar.ssl.TLSSocketFactory
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.report.Reporter
import cn.jpush.android.api.JPushInterface
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug


/**
 *
 * Created by AoEiuV020 on 2017.10.03-17:04:22.
 */
@Suppress("MemberVisibilityCanPrivate")
class App : MultiDexApplication(), AnkoLogger {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var ctx: Context
        /**
         * 用于app不同页面传递数据时的序列化，
         */
        val gson: Gson = GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create()

        lateinit var adRequest: AdRequest
    }

    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext

        // 初始化要放在用到JsonPath之前
        gsonJsonPathInit()

        initDataSources()

        // android4连接https可能抛SSLHandshakeException，
        // 是tls1.2没有启用，
        TLSSocketFactory.makeDefault()

        // 低版本api(<=20)默认不能用矢量图的selector, 要这样设置，
        // 还有ContextCompat.getDrawable也不行，
        // it's not a BUG, it's a FEATURE,
        // https://issuetracker.google.com/issues/37100284
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        initAdmob()

        initReporter()

        initJpush()

        initGlide()

    }

    private fun initGlide() {
        Glide.get(ctx).registry
    }

    private fun initDataSources() {
        DataManager.init(ctx)
    }

    private fun initJpush() {
        if (BuildConfig.DEBUG) {
            JPushInterface.setDebugMode(Log.isLoggable("JPush", Log.DEBUG))
            JPushInterface.setAlias(ctx, 0, "debug")
        }
        JPushInterface.init(ctx)
    }

    private fun initAdmob() {
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
    }

    /**
     * 初始化异常上报封装类，
     */
    private fun initReporter() {
        Reporter.init(ctx)
    }
}
