package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstIntPattern
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.06.03-22:25:57.
 */
class Gulizw : DslJsoupNovelContext() {init {
    site {
        name = "谷粒网"
        baseUrl = "http://www.gulizw.com"
        logo = "http://www.gulizw.com/images/logo.png"
    }
    search {
        get {
            // http://www.biqugebook.com/modules/article/search.php?searchtype=articlename&searchkey=%B6%BC%CA%D0&page=1
            charset = "GBK"
            url = "/modules/article/search.php"
            data {
                "searchtype" to "articlename"
                "searchkey" to it
                // 加上&page=1可以避开搜索时间间隔的限制，
                // 也可以通过不加载cookies避开搜索时间间隔的限制，
                "page" to "1"
            }
        }
        document {
            if (URL(root.ownerDocument().location()).path.startsWith("/book/")) {
                single {
                    name("div.info > h1 > a")
                    author("p.author > a")
                }
            } else {
                items("body > div.main > table > tbody > tr:not(:nth-child(1))") {
                    name("> td:nth-child(1) > a")
                    author("> td:nth-child(3)")
                }
            }
        }
    }
    // http://www.gulizw.com/book/94719.html
    bookIdRegex = firstIntPattern
    detailPageTemplate = "/book/%s.html"
    detail {
        document {
            novel {
                name("div.info > h1 > a")
                author("p.author > a")
            }
            image("div.con_limg > img")
            update("div.info > div.lastrecord", format = "yyyy-MM-dd", block = pickString("最新章节\\((.*)\\)"))
            introduction("div.info > div.r_cons")
        }
    }
    // http://www.gulizw.com/94719/
    chaptersPageTemplate = "/%s/"
    chapters {
        document {
            items("#novel94719 > dl > dd > a")
            // 时间是js拿到的，
            // http://www.gulizw.com/modules/article/52mb.php?id=94719&uptime=
        }
    }
    // http://www.gulizw.com/94719/28723558.html
    contentPageTemplate = "/%s.html"
    content {
        document {
            items("#content", block = ownLines())
        }.dropLastWhile { it == "：" }
    }
}
}

