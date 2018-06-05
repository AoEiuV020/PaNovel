package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstIntPattern
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.06.05-19:09:22.
 */
class Miaobige : DslJsoupNovelContext() {init {
    // 这网站章节列表页的章节不全，但是点下一章又能看的，这就没法爬了，
    enabled = false
    site {
        name = "妙笔阁"
        baseUrl = "https://www.miaobige.com"
        logo = "https://img.miaobige.com/skin/images/logo.png"
    }
    cookieFilter {
        removeAll { httpUrl.encodedPath().startsWith("/search/") }
    }
    search {
        // https://www.miaobige.com/search/?s=%B6%BC%CA%D0
        // 搜索有三秒限制，不能通过选择页码绕过，好像是服务器端的限制，不好触发，干脆清cookie
        get {
            url = "/search/"
            data {
                "s" to it
            }
        }
        document {
            if (URL(root.ownerDocument().location()).path.startsWith("/book/")) {
                single {
                    name(".booktitle > h1:nth-child(1)")
                    author("#author > a:nth-child(1)")
                }
            } else {
                items("#sitembox > dl") {
                    name("> dd:nth-child(2) > h3:nth-child(1) > a:nth-child(1)")
                    author("> dd:nth-child(3) > span:nth-child(1)")
                }
            }
        }
    }
    // https://www.miaobige.com/book/11114/
    bookIdRegex = firstIntPattern
    detailPageTemplate = "/book/%s/"
    detail {
        document {
            novel {
                name(".booktitle > h1:nth-child(1)")
                author("#author > a:nth-child(1)")
            }
            image("#bookimg > img:nth-child(1)")
            update(".uptime > span:nth-child(1)", format = "yyyy-MM-dd HH:mm:ss")
            introduction("#bookintro > p:nth-child(1)")
        }
    }
    // https://www.miaobige.com/read/11114/
    // 这网站有的小说章节列表分两页，说不定还有不止两页的，不好搞，
    // 而且有的章节在列表中没有地址，但是下一章又能找到，
    chapters {
        document {
            /*
            <a href="/book/1196/443990.html">第一章 觉醒日</a>
             */
            items("#list > dl > dd > a")
            lastUpdate("#info > p:nth-child(4)", format = "yyyy-MM-dd HH:mm:ss", block = pickString("最后更新：(.*)"))
        }
    }
    contentPageTemplate = "/book/%s.html"
    content {
        document {
            items("#content")
        }
    }
}
}

