package cc.aoeiuv020.panovel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import cc.aoeiuv020.panovel.api.paNovel
import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 *
 * Created by AoEiuV020 on 2017.10.03-17:04:22.
 */
@Suppress("MemberVisibilityCanPrivate")
class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var ctx: Context
        val gsonBuilder: GsonBuilder = GsonBuilder().paNovel()
        val gson: Gson = gsonBuilder.create()
    }

    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext
    }
}
