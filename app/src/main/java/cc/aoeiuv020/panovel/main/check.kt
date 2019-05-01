package cc.aoeiuv020.panovel.main

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import cc.aoeiuv020.jsonpath.get
import cc.aoeiuv020.jsonpath.jsonPath
import cc.aoeiuv020.okhttp.OkHttpUtils
import cc.aoeiuv020.okhttp.string
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.util.*
import cc.aoeiuv020.regex.compilePattern
import cc.aoeiuv020.regex.pick
import org.jetbrains.anko.*
import java.io.BufferedReader
import java.net.URL

/**
 *
 * Created by AoEiuV020 on 2018.03.25-04:00:29.
 */
object Check : Pref, AnkoLogger {
    override val name: String
        get() = "Check"
    private var cachedVersionName: String by Delegates.string("0")
    private const val CHANGE_LOG_URL = "https://raw.githubusercontent.com/AoEiuV020/PaNovel/master/app/src/main/assets/ChangeLog.txt"
    private const val COOLAPK_MARKET_PACKAGE_NAME = "com.coolapk.market"
    private var knownVersionName: String by Delegates.string("0")
    private fun getNewestVersionName(): String {
        return OkHttpUtils.get(LATEST_RELEASE_GITHUB).string().jsonPath.get("tag_name")
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getChangeLogFromAssert(ctx: Context, fromVersion: String): String {
        return try {
            ctx.assets.open("ChangeLog.txt").bufferedReader().cutChangeLog(fromVersion)
        } catch (e: Exception) {
            val message = "剪取更新日志失败，\n"
            message + e.message
        }
    }

    private fun getChangeLog(currentVersionName: String): String {
        return try {
            URL(CHANGE_LOG_URL).openStream().bufferedReader().cutChangeLog(currentVersionName)
        } catch (e: Exception) {
            val message = "获取更新日志失败，\n"
            message + e.message
        }
    }

    private fun BufferedReader.cutChangeLog(fromVersion: String): String {
        val pattern = compilePattern("([0-9.]*):")
        return useLines { line ->
            line.takeWhile {
                try {
                    val (versionName) = it.pick(pattern)
                    VersionUtil.compare(versionName, fromVersion) > 0
                } catch (_: Exception) {
                    true
                }
            }.joinToString("\n")
        }
    }

    /**
     * @param tip 无更新或者更新失败是否提示，
     */
    fun asyncCheckVersion(ctx: Context, tip: Boolean = false) {
        ctx.doAsync({ e ->
            val message = "检测更新失败，"
            Reporter.post(message, e)
            error(message, e)
            if (tip) {
                ctx.runOnUiThread {
                    ctx.toast(ctx.getString(R.string.tip_update_failed))
                }
            }
        }) {
            val currentVersionName = VersionUtil.getAppVersionName(ctx)
            val newestVersionName = getNewestVersionName()
            info {
                "checkVersion $currentVersionName/$newestVersionName"
            }
            val hasUpdate = VersionUtil.compare(newestVersionName, currentVersionName) > 0
                    && VersionUtil.compare(newestVersionName, knownVersionName) > 0
            val changeLog = when {
                // 有更新，网上获取更新日志，截取当前版本到网上最新的日志，
                hasUpdate -> {
                    getChangeLog(currentVersionName)
                }
                // 已经更新，截取更新部分日志，从上次保存的版本到最新的日志，
                VersionUtil.compare(currentVersionName, cachedVersionName) > 0 -> {
                    getChangeLogFromAssert(ctx, cachedVersionName)
                }
                // 没有更新也不是刚更新完，直接返回，
                else -> {
                    if (tip) {
                        uiThread { ctx ->
                            ctx.toast(ctx.getString(R.string.tip_no_new_version))
                        }
                    }
                    return@doAsync
                }
            }
            // 缓存当前版本，以便更新后对比，
            cachedVersionName = currentVersionName
            uiThread { ctx ->
                if (hasUpdate) {
                    ctx.alert {
                        title = "有更新"
                        message = changeLog
                        neutralPressed("忽略") {
                            knownVersionName = newestVersionName
                        }
                        positiveButton("酷安") {
                            startCoolapk(ctx)
                        }
                        negativeButton("Github") {
                            ctx.browse(RELEASE_GITHUB)
                        }
                    }.safelyShow()
                } else {
                    ctx.alert {
                        title = "已更新"
                        message = changeLog
                        yesButton { }
                    }.safelyShow()
                }
            }
        }
    }

    private fun startCoolapk(ctx: Context) {
        // 直接打开酷安市场app，
        // 如果不存在，改打开浏览器，
        val uri = Uri.parse("market://details?id=${ctx.packageName}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.`package` = COOLAPK_MARKET_PACKAGE_NAME
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            ctx.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            info { "没安装酷安app," }
            ctx.browse(RELEASE_COOLAPK)
        }
    }

    private const val AOEIUV020_SIGNATURE = "F473239FE5E994CC7FF64F505D0F0BB6F8E3CB8C"
    private const val RELEASE_COOLAPK = "https://www.coolapk.com/apk/167994"
    private const val RELEASE_GITHUB = "https://github.com/AoEiuV020/PaNovel/releases"
    private const val LATEST_RELEASE_GITHUB = "https://api.github.com/repos/AoEiuV020/PaNovel/releases/latest"
    private var ignoreSignatureCheck: Boolean by Delegates.boolean(false)
    private var signature: String by Delegates.string("")

    /**
     * @return 忽略或者通过都返回true,
     */
    private fun checkSignature(ctx: Context): Boolean {
        if (ignoreSignatureCheck) {
            return true
        }
        val apkSign = signature.takeIf(String::isNotEmpty)
                ?: SignatureUtil.getAppSignature(ctx).also { signature = it }
        return apkSign == AOEIUV020_SIGNATURE
    }

    fun asyncCheckSignature(ctx: Context) {
        ctx.doAsync({ e ->
            val message = "检查签名出错"
            error(message, e)
            Reporter.post(message, e)
        }) {
            if (checkSignature(ctx)) {
                return@doAsync
            }
            uiThread { ctx ->
                ctx.alert {
                    title = "签名不正确"
                    message = "你可能用了假app,"
                    neutralPressed("忽略") {
                        ignoreSignatureCheck = true
                    }
                    positiveButton("酷安") {
                        startCoolapk(ctx)
                    }
                    negativeButton("Github") {
                        ctx.browse(RELEASE_GITHUB)
                    }
                }.safelyShow()
            }
        }
    }
}
