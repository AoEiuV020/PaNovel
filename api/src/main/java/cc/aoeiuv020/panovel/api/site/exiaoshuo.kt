package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.textListSplitWhitespace
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.06.06-12:42:10.
 */
class Exiaoshuo : DslJsoupNovelContext() {init {
    hide = true
    site {
        name = "E小说"
        baseUrl = "https://www.exiaoshuo.cc"
        logo = "https://www.exiaoshuo.cc/images/logo.gif"
    }
    search {
        // https://www.exiaoshuo.cc/modules/article/search.php?searchkey=%B6%BC%CA%D0&page=1
        get {
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
            if (URL(root.ownerDocument().location()).path.startsWith("/intro/")) {
                single {
                    name(".r420 > h1:nth-child(1)")
                    author("span.black:nth-child(1)")
                }
            } else {
                items("ul.clearfix > li") {
                    name("> div:nth-child(2) > h3:nth-child(1) > a:nth-child(1)")
                    author("> div:nth-child(2) > div:nth-child(2) > span:nth-child(1)")
                }
            }
        }
    }
    // https://www.exiaoshuo.cc/intro/397
    // https://www.exiaoshuo.cc/xiaoshuo/0/397/index.html
    // https://www.exiaoshuo.cc/xiaoshuo/0/397/336169.html
    bookIdRegex = "/(intro|xiaoshuo/\\d+)/(\\d+)"
    bookIdIndex = 1
    detailPageTemplate = "/intro/%s"
    detail {
        document {
            novel {
                name(".r420 > h1:nth-child(1)")
                author("span.black:nth-child(1)")
            }
            image(".con_limg > img:nth-child(1)")
            update(".green", format = "yyyy-MM-dd")
            introduction(".r_cons > p:nth-child(1)") {
                it.textListSplitWhitespace().joinToString("\n") {
                    it.removeSuffix("...[详细介绍]")
                }
            }
        }
    }
    // https://www.exiaoshuo.cc/xiaoshuo/0/397/index.html
    chapterDivision = 1000
    chaptersPageTemplate = "/xiaoshuo/%d/%s/index.html"
    chapters {
        document {
            items(".clearfix > ul > li > a:nth-child(1)")
        }
    }
    // https://www.exiaoshuo.cc/xiaoshuo/0/397/336169.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/xiaoshuo/%s.html"
    content {
        document {
            /*
            <center><br><h2>记住E小说永久网址：www.exiaoshuo.cc  |   手机站：m.exiaoshuo.cc</h2><br></center>
             */
            items(".chapter_content", block = ownLines())
        }
    }
}
}

