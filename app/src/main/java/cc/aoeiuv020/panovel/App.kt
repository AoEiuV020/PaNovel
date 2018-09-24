package cc.aoeiuv020.panovel

import android.annotation.SuppressLint
import android.content.Context
import android.support.multidex.MultiDexApplication
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import cc.aoeiuv020.jsonpath.JsonPathUtils
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.ssl.TLSSocketFactory
import cc.aoeiuv020.ssl.TrustManagerUtils
import cn.jpush.android.api.JPushInterface
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import java.net.URL
import javax.net.ssl.HttpsURLConnection


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

        initJson()

        initDataSources()

        initSsl()

        initVector()

        initAdmob()

        initReporter()

        initJpush()

        initGlide()

        initJar()

    }

    /**
     * 初始化要放在用到JsonPath之前，
     */
    private fun initJson() {
        JsonPathUtils.initGson()
    }

    /**
     * 低版本api(<=20)默认不能用矢量图的selector, 要这样设置，
     * 还有ContextCompat.getDrawable也不行，
     * it's not a BUG, it's a FEATURE,
     * https://issuetracker.google.com/issues/37100284
     */
    private fun initVector() {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    /**
     * android4连接https可能抛SSLHandshakeException，各种毛病，
     * 只这样不能完全修复，但是app里主要是用okhttp3, 那边配置好了，
     */
    private fun initSsl() {
        HttpsURLConnection.setDefaultSSLSocketFactory(TLSSocketFactory(TrustManagerUtils.include(emptySet())))
    }

    private fun initJar() {
        // 禁用默认缓存连接，对任意URLConnection实例使用都可以，
        // 否则jar打开的文件不会被关闭，从而导致文件被覆盖了依然能读到旧文件，
        // 多少会影响性能，
        URL("jar:file:/fake.jar!/fake.file").openConnection().defaultUseCaches = false
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
            val id = JPushInterface.getRegistrationID(ctx)
            info { "JPush registration id: <$id>" }
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
