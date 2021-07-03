@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.textList
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import java.net.URL

class Kssw : DslJsoupNovelContext() {init {
    site {
        name = "看书小说"
        baseUrl = "https://www.kssw.net"
        logo = "https://www.kssw.net/qqtxt/css/images/logo.gif"
    }
    // https://www.kssw.net/modules/article/search.php?searchtype=articlename&searchkey=%CE%D2%D5%E6%B2%BB%CA%C7%D0%B0%C9%F1%D7%DF%B9%B7&page=1
    // 这家是罕见的通过ip判断重复搜索的，
    search {
        get {
            charset = "GBK"
            url = "/modules/article/search.php"
            data {
                "searchtype" to "articlename"
                "searchkey" to it
                "page" to "1"
            }
        }
        document {
            if (URL(
                    root.ownerDocument().location()
                ).path.startsWith("/modules/article/search.php")
            ) {
                items("table.grid > tbody > tr:not(:nth-child(1))") {
                    name("> td:nth-child(1) > a")
                    author("> td:nth-child(3)")
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

