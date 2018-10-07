package cc.aoeiuv020.panovel.migration.impl

import android.content.Context
import cc.aoeiuv020.gson.GsonUtils
import cc.aoeiuv020.gson.toBean
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.migration.Migration
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.util.VersionName
import cc.aoeiuv020.panovel.util.notNullOrReport
import com.google.gson.Gson
import okhttp3.Cookie
import okhttp3.HttpUrl
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error

/**
 * 迁移旧版的内嵌浏览器登录状态，
 * 2.2.0开始有内嵌浏览器，
 * cookies原本是存在缓存cacheDir里的，
 * 2.2.2开始移到filesDir里，
 * Created by AoEiuV020 on 2018.05.17-17:28:13.
 */
class LoginMigration : Migration(), AnkoLogger {
    override val to: VersionName = VersionName("2.2.2")
    override val message: String = "登录状态，"

    override fun migrate(ctx: Context, from: VersionName) {
        debug {
            "migrate from: ${from.name}"
        }
        if (from < VersionName("2.2.0")) {
            // 2.2.0 之前没有内嵌浏览器，不用迁移登录状态，
            // 但是，2.2.2才开始记录版本号，所以传入的会是"0", 不能跳过，
        }
        val cacheDir = ctx.cacheDir.resolve("api")
        if (!cacheDir.exists()) {
            // 没有数据就不继续了，
            return
        }
        val fileList = cacheDir.list()
        if (fileList.isEmpty()) {
            // 目录里没有网站的记录就不继续了，
            return
        }
        // 用于存取cookies, 和2.2.0版本一样配置的gson，
        val gson: Gson = GsonUtils.gsonBuilder
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create()
        // 以前的缓存目录名是网站上下文的类名，
        // 而且我还开了混淆，悲剧，
        // 这是2.2.1的混淆结果，
        val nameMap = mapOf(
                "飘天文学" to "c",
                "笔趣阁" to "a",
                "溜达小说" to "Liudatxt",
                "起点中文" to "Qidian",
                "动漫之家" to "b",
                "SF轻小说" to "f",
                "少年文学" to "g",
                "31小说" to "h",
                "幼狮书盟" to "i",
                "齐鲁文学" to "e"
        )
        DataManager.allNovelContexts().forEach { novelContext ->
            // 新网站不在map里就跳过，
            val fileName = nameMap[novelContext.site.name].also {
                debug {
                    "判断<${novelContext.site.name}, $it>"
                }
            } ?: return@forEach
            if (!fileList.contains(fileName)) {
                // 如果当前网站的缓存目录不存在就跳过这个网站，
                return@forEach
            }
            try {
                val oldCookiesFile = cacheDir.resolve(fileName)
                        .resolve("cookies")
                if (!oldCookiesFile.exists()) {
                    // 如果当前网站的cookies文件不存在就跳过这个网站，
                    return@forEach
                }
                val cookies: Map<String, String> = oldCookiesFile.readText().toBean(gson)
                debug {
                    "${novelContext.site.name}: $cookies"
                }
                // 导入旧版本cookies，
                val httpUrl = HttpUrl.parse(novelContext.site.baseUrl).notNullOrReport()
                novelContext.putCookies(cookies.mapValues { (name, value) ->
                    Cookie.parse(httpUrl, "$name=$value").notNullOrReport()
                })
            } catch (e: Exception) {
                // 单个网站处理失败正常继续，不抛异常，只上报异常，可能是这个网站数据被其他原因破坏了，
                val message = "网站<${novelContext.site.name}>登录状态迁移失败，"
                Reporter.post(message, e)
                error(message, e)
            }
        }
    }
}