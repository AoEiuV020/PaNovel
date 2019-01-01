package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext

/**
 * Created by AoEiuV020 on 2018.06.03-20:22:10.
 */
// 这个曾经是个轻小说网站，貌似已经没了，
@Suppress("unused")
class N360dxs : DslJsoupNovelContext() {init {
    hide = true
    site {
        name = "360°C小说"
        baseUrl = "http://www.360dxs.com"
        logo = "https://imgsa.baidu.com/forum/w%3D580/sign=fb5951156081800a6ee58906813433d6/96d7bd9759ee3d6d7ffa2f514f166d224e4ade53.jpg"
    }
    search {
        get {
            // http://www.360dxs.com/list.html?keyword=overlord
            url = "/list.html"
            data {
                "keyword" to it
            }
        }
        document {
            items("#book-list > li") {
                /*
                <a href="//jiaochuanwenku.360dxs.com/book_2394.html" itemprop="name">残缺都市的机械月姬</a>
                 */
                name("> div > div:nth-child(2) > b > a")
                author("> div > div:nth-child(3) > div:nth-child(1) > a")
            }
        }
    }
    // 不同文库的小说放在不同三级域名，不能取出bookId,
    bookIdRegex = null
    detail {
        document {
            novel {
                name("h1[itemprop=name]")
                author("a[itemprop=author]")
            }
            image("img[itemprop=image]")
            update("span[itemprop=dateModified]", format = "yyyy-MM-dd HH:mm:ss")
            introduction("div[itemprop=description]")
        }
    }
    chapters {
        document {
            /*
            <a class="am-text-truncate" href="http://jiaochuanwenku.360dxs.com/chapter_56213.html">序章</a>
             */
            items("div.am-u-sm-12 > div > div > ul > li > a")
            lastUpdate("span[itemprop=dateModified]", format = "yyyy-MM-dd HH:mm:ss")
        }
    }
    // 不同文库的小说放在不同三级域名，不能取出bookId,
    bookIdWithChapterIdRegex = null
    content {
        document {
            items("pre.book-content")
        }
    }
}
}

