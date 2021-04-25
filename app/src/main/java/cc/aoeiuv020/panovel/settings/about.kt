@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.settings

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.main.Check
import cc.aoeiuv020.panovel.server.ServerManager
import cc.aoeiuv020.panovel.util.VersionUtil
import cc.aoeiuv020.panovel.util.notNullOrReport
import cc.aoeiuv020.panovel.util.safelyShow
import cc.aoeiuv020.regex.compilePattern
import cc.aoeiuv020.regex.pick
import kotlinx.android.synthetic.main.content_about.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse
import org.jetbrains.anko.email
import org.jetbrains.anko.yesButton

/**
 *
 * Created by AoEiuV020 on 2017.11.23-10:26:44.
 */
class AboutFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.content_about, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val currentVersionName = VersionUtil.getAppVersionName(activity)
        tvVersion.text = currentVersionName
        tvEmail.setOnClickListener {
            email(tvEmail.text.toString(),
                    "${activity.getString(R.string.feedback)}[${activity.getString(R.string.app_name)}]$currentVersionName")
        }
        // 可能没有连接上服务器，就用固定的群号，
        val number = ServerManager.config?.qqGroup ?: tvGroup.text.toString()
        llGroup.setOnClickListener {
            val urlQQ = "mqqwpa://im/chat?chat_type=group&uin=${number}&version=1"
            browse(urlQQ)
        }
        llTelegram.setOnClickListener {
            browse("https://t.me/${tvTelegram.text}")
        }
        tvChangeLog.text = activity.assets.open("ChangeLog.txt").reader().readText()

        tvLicenses.setOnClickListener {
            // [jsoup](https://github.com/jhy/jsoup)
            val pattern = compilePattern("\\[(\\S*)\\]\\((\\S*)\\)")
            val (nameList, linkList) = activity.assets.open("Licenses.txt").reader().readLines()
                    .mapNotNull {
                        try {
                            val (name, link) = it.pick(pattern)
                            Pair(name, link)
                        } catch (_: Exception) {
                            null
                        }
                    }.unzip()
            activity.alert {
                title = activity.getString(R.string.library)
                items(nameList) { _, i ->
                    activity.browse(linkList[i])
                }
                yesButton { it.dismiss() }
            }.safelyShow()
        }

        tvUpdate.setOnClickListener {
            // 异步检查是否有更新，
            Check.asyncCheckVersion(activity.notNullOrReport(), true)
        }
    }
}