package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.notNull
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.06.03-15:46:06.
 */
class Ggdown : DslJsoupNovelContext() {init {
    // 网络连接超时，默认不启用，
    enabled = false
    site {
        name = "格格党"
        baseUrl = "http://www.ggdown.com"
        logo = "http://www.ggdown.com/themes/yssm/logo.gif"
    }
    search {
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
            if (URL(root.ownerDocument().location()).path.startsWith("/books/")) {
                single {
                    name("#content > div:nth-child(1) > div > div.book-info > div.book-title > h1")
                    author("#content > div:nth-child(1) > div > div.book-info > div.book-title > em", block = pickString("作者：(\\S*)"))
                }
            } else {
                items("#content > div.inner > div.details > div.item-pic") {
                    name("> h3 > a")
                    author("> p:nth-child(3) > i:nth-child(1)")
                }
            }
        }
    }
    // http://www.ggdown.com/books/19447.html
    detailPageTemplate = "/books/%s.html"
    detail {
        document {
            novel {
                name("#content > div:nth-child(1) > div > div.book-info > div.book-title > h1")
                author("#content > div:nth-child(1) > div > div.book-info > div.book-title > em", block = pickString("作者：(\\S*)"))
            }
            image("#content > div:nth-child(1) > div > div.book-img > img")
            /*
            <p class="book-stats">
                            <b>状态：</b>连载中&nbsp;&nbsp;<b>字数：</b>1243532&nbsp;&nbsp;<b>更新时间：</b>2018/2/12 10:20:33</p>
             */
            update("#content > div:nth-child(1) > div > div.book-info > p.book-stats", format = "yyyy/MM/dd HH:mm:ss", block = pickString("更新时间：(.*)"))
            /*
<p class="book-intro">黑风城战记是一本非常优秀的穿越小说，由格格党（http://www.ggdown.com）转载更新，并且已经更新到最新章节第226章 【白冢谷】,版权归原作者耳雅所有<br>    《黑风城战记》是《龙图案卷集》的续篇，由十个战役组成，地点是西北要塞黑风城，同时也有破案情节贯穿于战役中~~
    恶帝城的建立打破了西北的平静，正邪之战一触即发~~
    案件结合战役，龙图原班人马继续他们的传奇经历~~
</p>
             */
            introduction("#content > div:nth-child(1) > div > div.book-info > p.book-intro") {
                it.textNodes().drop(1).joinToString("\n") {
                    it.ownLinesString()
                }
            }
        }
    }
    // http://www.ggdown.com/19/19447/index.html
    chapterDivision = 1000
    chaptersPageTemplate = "/%d/%s/index.html"
    chapters {
        document {
            items("#main > div > dl > dd > a")
        }
    }
    // http://www.ggdown.com/19/19447/22928333.html
    // 网页内容不全，要拿api,
    // http://www.ggdown.com/t/t.php?id=/19&did=19447&vid=5585742
    // http://www.ggdown.com/t/t.php?did=19447&vid=5585742
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/t/t.php?did=%s&vid=%s"
    getNovelContentUrl {
        val (_, did, vid) = findBookIdWithChapterId(it).split("/")
        contentPageTemplate.notNull().format(did, vid)
    }
    content {
        document {
            // 有的有广告，
            // http://www.ggdown.com/t/t.php?did=19447&vid=5585742
            items("body", block = ownLines())
        }
    }
}
}

