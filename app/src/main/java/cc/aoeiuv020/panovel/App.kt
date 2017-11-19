package cc.aoeiuv020.panovel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import cc.aoeiuv020.panovel.api.paNovel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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

        // 无视RxJava抛的异常，也就是不被捕获调用onError的异常，
        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer())

        MobileAds.initialize(this, "ca-app-pub-3036112914192534~4631187497")
        adRequest = AdRequest.Builder()
                .also { builder ->
                    // 添加广告的测试设备，
                    try {
                        // 通过反射确保文件不存在也没问题，
                        val id: Int = Class.forName("${R::class.java.name}\$raw").getField("admob_test_device_list").get(null) as Int
                        resources.openRawResource(id).reader().readLines().filter(String::isNotBlank).forEach {
                            debug { "add test device: $it" }
                            builder.addTestDevice(it)
                        }
                    } catch (_: Exception) {
                        // 什么都不做，
                    }
                }.build()

    }
}
