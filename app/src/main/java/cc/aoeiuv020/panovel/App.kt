package cc.aoeiuv020.panovel

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Process
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import cc.aoeiuv020.gson.GsonUtils
import cc.aoeiuv020.jsonpath.JsonPathUtils
import cc.aoeiuv020.panovel.ad.AdHelper
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.settings.AdSettings
import cc.aoeiuv020.panovel.util.DnsUtils
import cc.aoeiuv020.ssl.TLSSocketFactory
import cc.aoeiuv020.ssl.TrustManagerUtils
import com.bumptech.glide.Glide
import com.google.gson.Gson
import org.jetbrains.anko.AnkoLogger
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.properties.Delegates


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
         * 当前进程是否主进程，部分操作需要判断只在主进程执行一次，
         */
        var isMainProcess: Boolean by Delegates.notNull()

        /**
         * 用于app不同页面传递数据时的序列化，
         */
        val gson: Gson = GsonUtils.gsonBuilder
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()
    }

    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext
        isMainProcess = isMainProcess()

        initDnsUtils()

        initJson()

        initDataSources()

        initSsl()

        initVector()

        initReporter()

        initAd()

        initGlide()

        initJar()

    }

    private fun initDnsUtils() {
        DnsUtils.init(ctx)
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
     *
     * 这个设置只对AppCompatActivity有效，其他context没用，
     */
    private fun initVector() {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    /**
     * android4连接https可能抛SSLHandshakeException，各种毛病，
     * 只这样不能完全修复，但是app里主要是用okhttp3, 那边配置好了，
     */
    private fun initSsl() {
        HttpsURLConnection.setDefaultSSLSocketFactory(
            TLSSocketFactory(
                TrustManagerUtils.include(
                    emptySet()
                )
            )
        )
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

    /**
     * 初始化异常上报封装类，
     */
    private fun initReporter() {
        Reporter.init(ctx)
    }

    private fun initAd() {
        if (AdSettings.adEnabled) {
            AdHelper.init(this)
        }
    }

    private fun getCurrentProcessName(): String {
        val pid = Process.myPid()
        var processName = ""
        val manager = applicationContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (process in manager.runningAppProcesses) {
            if (process.pid == pid) {
                processName = process.processName
            }
        }
        return processName
    }

    private fun isMainProcess(): Boolean {
        return applicationContext.packageName == getCurrentProcessName()
    }

}
