@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.textList
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import java.net.URL

class Kssw : DslJsoupNovelContext() {init {
    reason = "网站关了,"
    hide = true
    site {
        name = "看书小说"
        baseUrl = "https://pc.kssw.net"
        logo = "https://pc.kssw.net/qqtxt/css/images/logo.gif"
    }
    // 电脑版禁止搜索了，直接提示宝塔识别成攻击，
    // https://wap.kssw.net/s.php
    search {
        post {
            charset = "GBK"
            url = "//wap.kssw.net/s.php"
            data {
                "search_key" to it
            }
        }
        document {
            if (URL(
                    root.ownerDocument().location()
                ).path.startsWith("/s.php")
            ) {
                items("div.searchresult > p") {
                    name("> a")
                    author("> span > a")
                }
            } else {
                single {
                    name("div.jieshao > div.rt > h1")
                    author(
                        "div.jieshao > div.rt > div.msg > em:nth-child(1)",
                        block = pickString("作者：(\\S*)")
                    )
                }
            }
        }
    }
    detailPageTemplate = "/book/%s/"
    detail {
        document {
            novel {
                name("div.jieshao > div.rt > h1")
                author(
                    "div.jieshao > div.rt > div.msg > em:nth-child(1)",
                    block = pickString("作者：(\\S*)")
                )
            }
            image("div.jieshao > div.lf > img")
            update(
                "div.jieshao > div.rt > div.msg > em:nth-child(3)",
                format = "yyyy-MM-dd HH:mm",
                block = pickString("更新时间：(.*)")
            )
            introduction("div.intro")
        }
    }
    chapters {
        document {
            items("div.mulu > ul > li > a")
            lastUpdate(
                "div.jieshao > div.rt > div.msg > em:nth-child(3)",
                format = "yyyy-MM-dd HH:mm",
                block = pickString("更新时间：(.*)")
            )
        }
    }
    contentPageTemplate = "/book/%s.html"
    content {
        document {
            items(
                "div.yd_text2",
                block = {
                    it.textList().map { line -> line.removeSuffix("(m.看书小说）更新最快，小哥哥小姐姐记得收藏哦！") }
                })
        }
    }
}
}

