package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ownTextListSplitWhitespace
import cc.aoeiuv020.base.jar.ownTextListWithImage
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern

/**
 * Created by AoEiuV020 on 2018.06.06-17:07:19.
 */
class Qingkan5 : DslJsoupNovelContext() {init {
    // 这网站可能有图片，搞不定，
    // 还有空正文章节，
    // 墙内连不上，墙外400,
    // 废了，
    hide = true
    site {
        name = "请看小说5"
        baseUrl = "http://www.qingkan5.com"
        logo = "http://www.qingkan5.com/skin/images/logo.png"
    }
    search {
        get {
            // http://www.qingkan5.com/modules/article/search.php?searchtype=articlename&searchkey=%B6%BC%CA%D0&page=1
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
            single("^/shu/\\d+\\.html$") {
                name("#bookinfo > div.bookright > h1")
                author("#author > a")
            }
            items("#nr") {
                name("> td:nth-child(1) > a")
                author("#nr > td:nth-child(3)")
            }
        }
    }
    // http://www.qingkan5.com/shu/5518.html
    // http://www.qingkan5.com/html/5/5518/index.html
    // http://www.qingkan5.com/html/5/5518/12254884.html
    bookIdRegex = "/(shu|html/\\d+)/(\\d+)"
    bookIdIndex = 1
    detailPageTemplate = "/shu/%s.html"
    detail {
        document {
            novel {
                name("#bookinfo > div.bookright > h1")
                author("#author > a")
            }
            image("#bookimg > img")
            update("#bookinfo > div.bookright > div.new > span.new_t", format = "yyyy-MM-dd", block = pickString("最后更新：(.*)"))
            introduction("#bookintro > p") {
                it.textNodes().flatMap { it.ownTextListSplitWhitespace() }
                        .dropLastWhile {
                            it == "各位书友要是觉得《${novel?.name}》还不错的话请不要忘记向您QQ群和微博里的朋友推荐哦！"
                        }.joinToString("\n")
            }
        }
    }
    // http://www.qingkan5.com/html/5/5518/index.html
    chapterDivision = 1000
    chaptersPageTemplate = "/html/%d/%s/index.html"
    chapters {
        document {
            items("ul.mulu_list > li > a")
        }
    }
    // http://www.qingkan5.com/html/5/5518/12254884.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/html/%s.html"
    content {
        document {
            // http://www.qingkan5.com/html/0/342/12233891.html
            // 这网站可能有图片，搞不定，
            // 图片在#content下的div里，
            // 最底下有多余的链接，
            /*
            <a href="http://www.qingkan5.com/html/0/342/index.html" title="《烽烟狼卷》最新章节">《烽烟狼卷》最新章节</a>
             */
            items("#center #content, div.divimage") {
                it.ownTextListWithImage()
            }
        }
    }
}
}

