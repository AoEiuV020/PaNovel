package cc.aoeiuv020.panovel.share

import cc.aoeiuv020.base.jar.jsoupConnect

/**
 * 网上贴文本，免费还无限制，
 *
 * Created by AoEiuV020 on 2018.03.07-19:16:41.
 */
internal class PasteUbuntu {
    companion object {
        const val homePage = "https://paste.ubuntu.com/"
    }

    fun check(url: String): Boolean {
        return url.startsWith(homePage)
    }

    fun download(url: String): String {
        val root = jsoupConnect(url)
        return root.select("#contentColumn > div > div > div > table > tbody > tr > td.code > div > pre").first().text()
    }

}
