package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.06.03-20:06:02.
 */
class Yipinxia : DslJsoupNovelContext() {init {
    site {
        name = "一品侠小说"
        baseUrl = "http://www.yipinxia.net"
        logo = "http://www.yipinxia.net.img.800cdn.com/images/logo.png"
    }
    search {
        get {
            // http://www.yipinxia.net/modules/article/search.php?searchkey=%B6%BC%CA%D0
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
            if (URL(root.ownerDocument().location()).path.startsWith("/yuedu/")) {
                single {
                    name("#content > div.body > div.container > div.contents.ks-clear > div.bookk > div.bookk-info.ks-clear > div > h2 > a")
                    author("#content > div.body > div.container > div.contents.ks-clear > div.bookk > div.bookk-info.ks-clear > div > p.intr", block = pickString("作者：(\\S*)"))
                }
            } else {
                items("#content > div > table > tbody > tr:not(:nth-child(1))") {
                    name("> td:nth-child(1) > a")
                    author("> td:nth-child(3)")
                }
            }
        }
    }
    // http://www.yipinxia.net/yuedu/5542/
    detailPageTemplate = "/yuedu/%s/"
    detail {
        document {
            novel {
                name("#content > div.body > div.container > div.contents.ks-clear > div.bookk > div.bookk-info.ks-clear > div > h2 > a")
                author("#content > div.body > div.container > div.contents.ks-clear > div.bookk > div.bookk-info.ks-clear > div > p.intr", block = pickString("作者：(\\S*)"))
            }
            image("#BookImage")
            introduction("#content > div.body > div.container > div.contents.ks-clear > div.bookk > div.bookk-info.ks-clear > div > p.con")
        }
    }
    // http://www.yipinxia.net/shu/5542/
    chaptersPageTemplate = "/shu/%s/"
    chapters {
        document {
            items("body > div.book > div.list > ul > li > a")
        }
    }
    // http://www.yipinxia.net/shu/5542/1227954.html
    contentPageTemplate = "/shu/%s.html"
    content {
        document {
            items("#booktext")
        }
    }
}
}

